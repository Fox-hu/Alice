package com.component.compiler.processor;

import com.component.compiler.Constants;
import com.component.compiler.utils.Logger;
import com.component.compiler.utils.TypeUtils;
import com.component.router.annotation.RouteNode;
import com.component.router.enums.NodeType;
import com.component.router.model.Node;
import com.component.router.utils.RouteUtils;
import com.google.auto.service.AutoService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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

    /**
     * 注解处理器是运行在另外一个jvm之中，如果发生异常则crash信息晦涩难懂 所以提供一个
     * messager进行信息处理给其他使用处理器的进程
     */
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

    private void generateRouterTable() {
        String clzName = RouteUtils.genHostUIRouterClass(host);

        //pkg
        String pkg = clzName.substring(0, clzName.lastIndexOf("."));

        //simpleName
        String simpleName = clzName.substring(clzName.lastIndexOf(".") + 1);

//        ClassName.get(elements.getTypeElement())

    }

    private void generateRouterImpl() {

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
