package org.jahia.modules.customGql;

import graphql.ErrorType;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import org.jahia.bin.Render;
import org.jahia.modules.graphql.provider.dxm.BaseGqlClientException;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeImpl;
import org.jahia.modules.graphql.provider.dxm.node.NodeHelper;
import org.jahia.modules.graphql.provider.dxm.node.SpecializedTypesHandler;
import org.jahia.modules.graphql.provider.dxm.render.RenderNodeExtensions;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.settings.SettingsBean;
import pl.touk.throwing.ThrowingFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GraphQLTypeExtension(GqlJcrNode.class)
public class RenderTreeNodeExtension {

    private GqlJcrNode node;

    public RenderTreeNodeExtension(GqlJcrNode node) {
        this.node = node;
    }

    @GraphQLField
    public List<GqlJcrNode> getRenderChildren(@GraphQLName("view") String view, @GraphQLName("templateType") String templateType, @GraphQLName("contextConfiguration") String contextConfiguration,
                                              @GraphQLName("language") String language, DataFetchingEnvironment environment) {
        List<GqlJcrNode> nodes = new ArrayList<>();
        try {
            RenderService renderService = (RenderService) SpringContextSingleton.getBean("RenderService");

            if (contextConfiguration == null) {
                contextConfiguration = "preview";
            }
            if (templateType == null) {
                templateType = "html";
            }

            if (language == null) {
                language = node.getNode().getResolveSite().getDefaultLanguage();
                if (language == null) {
                    language = "en";
                }
            }

            HttpServletRequest request = ((GraphQLContext) environment.getContext()).getRequest().get();
            if (request instanceof HttpServletRequestWrapper) {
                request = (HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest();
            }
            HttpServletResponse response = ((GraphQLContext) environment.getContext()).getResponse().get();

            Map<GqlJcrNode, List<Resource>> gqlNodeToResources = (Map<GqlJcrNode, List<Resource>>) request.getAttribute("gqlNodeToResources");
            if (gqlNodeToResources == null || !gqlNodeToResources.containsKey(node)) {
                Map<Resource, List<Resource>> resourcesMap = new LinkedHashMap<>();
                gqlNodeToResources = new LinkedHashMap<>();
                request.setAttribute("gqlRenderChildren", resourcesMap);
                request.setAttribute("gqlNodeToResources", gqlNodeToResources);
                resourcesMap.put(null, new ArrayList<>());

                JCRNodeWrapper node = NodeHelper.getNodeInLanguage(this.node.getNode(), language);

                Resource r = new Resource(node, templateType, view, contextConfiguration);

                RenderContext renderContext = new RenderContext(request, response, JCRSessionFactory.getInstance().getCurrentUser());
                renderContext.setMainResource(r);

                renderContext.setServletPath(Render.getRenderServletPath());

                JCRSiteNode site = node.getResolveSite();
                renderContext.setSite(site);

                response.setCharacterEncoding(SettingsBean.getInstance().getCharacterEncoding());

               String html = renderService.render(r, renderContext);

                List<Resource> resources = resourcesMap.get(null);
                for (Resource resource : resources) {
                    GqlJcrNode n = SpecializedTypesHandler.getNode(resource.getNode());
                    gqlNodeToResources.put(n, resourcesMap.get(resource));
                    nodes.add(n);
                }
            } else {
                Map<Resource, List<Resource>> resourcesMap = (Map<Resource, List<Resource>>) request.getAttribute("gqlRenderChildren");

                List<Resource> resources = gqlNodeToResources.get(node);
                for (Resource resource : resources) {
                    GqlJcrNode n = SpecializedTypesHandler.getNode(resource.getNode());
                    gqlNodeToResources.put(n, resourcesMap.get(resource));
                    nodes.add(n);
                }
            }
            return nodes;
//            return resourcesMap.get(null).stream().map((child) -> new RenderTreeNode(child, resourcesMap)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }


    @GraphQLField
    public String getParentPathInTemplate(DataFetchingEnvironment environment) {
        HttpServletRequest request = ((GraphQLContext) environment.getContext()).getRequest().get();

        Map<Resource, List<Resource>> map = (Map<Resource, List<Resource>>) request.getAttribute("gqlRenderChildren");

        for (Map.Entry<Resource, List<Resource>> entry : map.entrySet()) {
            for (Resource resource :  entry.getValue()) {
                if (resource.getNode().getPath().equals(node.getPath()) && entry.getKey() != null) {
                    return entry.getKey().getNode().getPath();
                }
            }
        }
        return "";
    }
}