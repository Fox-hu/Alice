package com.component.compiler.processor;

import com.component.compiler.Constants;
import com.component.compiler.utils.FileUtils;
import com.component.compiler.utils.Logger;
import com.component.compiler.utils.TypeUtils;
import com.component.router.annotation.RouteNode;
import com.component.router.enums.NodeType;
import com.component.router.model.Node;
import com.component.router.utils.RouteUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author fox.hu
 * @date 2018/9/20
 * 详细见https://www.race604.com/annotation-processing/
 */
@AutoService(Processor.class)
@SupportedOptions(Constants.KEY_HOST_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE_NODE)
public class RouteProcessor extends AbstractProcessor {
    private static final String mRouteMapperFieldName = "routeMapper";
    private static final String mParamsMapperFieldName = "paramsMapper";

    private Logger logger;

    private Filer mFiler;
    private Types types;
    private Elements elements;

    private TypeMirror type_String;

    private ArrayList<Node> routerNodes;
    private TypeUtils typeUtils;
    private String host = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        routerNodes = new ArrayList<>();
        mFiler = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();
        typeUtils = new TypeUtils(types, elements);

        type_String = elements.getTypeElement("java.lang.String").asType();

        logger = new Logger(processingEnv.getMessager());

        Map<String, String> options = processingEnvironment.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            host = options.get(Constants.KEY_HOST_NAME);
            logger.info(">>> host is " + host + " <<<");
        }
        if (host == null || host.equals("")) {
            host = "default";
        }
        logger.info(">>> RouteProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> routeNodes = roundEnvironment.getElementsAnnotatedWith(
                    RouteNode.class);
            try {
                parseRouteNodes(routeNodes);
            } catch (Exception e) {
                logger.error(e);
            }

            generateRouterImpl();
            generateRouterTable();
            return true;
        }
        return false;
    }

    private void generateRouterImpl() {
        String claName = RouteUtils.genHostUIRouterClass(host);

        //pkg
        String pkg = claName.substring(0, claName.lastIndexOf("."));
        //simpleName
        String cn = claName.substring(claName.lastIndexOf(".") + 1);
        // superClassName
        ClassName superClass = ClassName.get(elements.getTypeElement(Constants.BASECOMPROUTER));

        MethodSpec initHostMethod = generateInitHostMethod();
        MethodSpec initMapMethod = generateInitMapMethod();

        try {
            JavaFile.builder(pkg, TypeSpec.classBuilder(cn).addModifiers(PUBLIC).superclass(
                    superClass).addMethod(initHostMethod).addMethod(initMapMethod).build()).build()
                    .writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateRouterTable() {
        String fileName = RouteUtils.genRouterTable(host);
        if (FileUtils.createFile(fileName)) {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("auto generated, do not change !!!! \n\n");
            stringBuilder.append("HOST : " + host + "\n\n");

            for (Node node : routerNodes) {
                stringBuilder.append(node.getDesc() + "\n");
                stringBuilder.append(node.getPath() + "\n");
                Map<String, String> paramsType = node.getParamsDesc();
                if (MapUtils.isNotEmpty(paramsType)) {
                    for (Map.Entry<String, String> types : paramsType.entrySet()) {
                        stringBuilder.append(types.getKey() + ":" + types.getValue() + "\n");
                    }
                }
                stringBuilder.append("\n");
            }
            FileUtils.writeStringToFile(fileName, stringBuilder.toString(), false);
        }
    }

    /**
     * create init host method
     */
    private MethodSpec generateInitHostMethod() {
        TypeName returnType = TypeName.get(type_String);

        MethodSpec.Builder openUriMethodSpecBuilder = MethodSpec.methodBuilder("getHost").returns(
                returnType).addAnnotation(Override.class).addModifiers(Modifier.PUBLIC);

        openUriMethodSpecBuilder.addStatement("return $S", host);

        return openUriMethodSpecBuilder.build();
    }

    private MethodSpec generateInitMapMethod() {
        TypeName returnType = TypeName.VOID;

        MethodSpec.Builder openUriMethodSpecBuilder = MethodSpec.methodBuilder("initMap").returns(
                returnType).addAnnotation(Override.class).addModifiers(Modifier.PUBLIC);

        openUriMethodSpecBuilder.addStatement("super.initMap()");

        for (Node node : routerNodes) {
            openUriMethodSpecBuilder.addStatement(mRouteMapperFieldName + ".put($S,$T.class)",
                    node.getPath(), ClassName.get((TypeElement) node.getRawType()));

            // Make map body for paramsType
            StringBuilder mapBodyBuilder = new StringBuilder();
            Map<String, Integer> paramsType = node.getParamsType();
            if (MapUtils.isNotEmpty(paramsType)) {
                for (Map.Entry<String, Integer> types : paramsType.entrySet()) {
                    mapBodyBuilder.append("put(\"").append(types.getKey()).append("\", ").append(
                            types.getValue()).append("); ");
                }
            }
            String mapBody = mapBodyBuilder.toString();
            logger.info(">>> mapBody: " + mapBody + " <<<");
            if (!StringUtils.isEmpty(mapBody)) {
                openUriMethodSpecBuilder.addStatement(mParamsMapperFieldName + ".put($T.class," +
                                                      "new java.util.HashMap<String, Integer>(){{" +
                                                      mapBody + "}}" + ")",
                        ClassName.get((TypeElement) node.getRawType()));
            }
        }

        return openUriMethodSpecBuilder.build();
    }

    private void parseRouteNodes(Set<? extends Element> routeNodes) {
        TypeMirror activityType = elements.getTypeElement(Constants.ACTIVITY).asType();
        for (Element element : routeNodes) {
            TypeMirror tm = element.asType();
            RouteNode route = element.getAnnotation(RouteNode.class);

            if (types.isSubtype(tm, activityType)) {
                logger.info(">>> Found activity route: " + tm.toString() + " <<<");

                Node node = new Node();
                String path = route.path();

                checkPath(path);

                node.setPath(path);
                node.setDesc(route.desc());
                node.setPriority(route.priority());
                node.setNodeType(NodeType.ACTIVITY);
                node.setRawType(element);

                Map<String, Integer> paramsType = new HashMap<>();
                Map<String, String> paramsDesc = new HashMap<>();

                node.setParamsType(paramsType);
                node.setParamsDesc(paramsDesc);

                if (!routerNodes.contains(node)) {
                    routerNodes.add(node);
                }
            } else {
                throw new IllegalStateException("only activity can be annotated by RouteNode");
            }
        }
    }

    private void checkPath(String path) {
        if (path == null || path.isEmpty() || !path.startsWith("/")) {
            throw new IllegalArgumentException(
                    "path cannot be null or empty,and should start with /,this is:" + path);
        }

        if (path.contains("//") || path.contains("&") || path.contains("?")) {
            throw new IllegalArgumentException(
                    "path should not contain // ,& or ?,this is:" + path);
        }

        if (path.endsWith("/")) {
            throw new IllegalArgumentException(
                    "path should not endWith /,this is:" + path + ";or append a token:index");
        }
    }
}
