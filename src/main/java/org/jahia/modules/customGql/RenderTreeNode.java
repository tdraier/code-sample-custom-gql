package org.jahia.modules.customGql;

import graphql.ErrorType;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.BaseGqlClientException;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.SpecializedTypesHandler;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenderTreeNode {

    private Resource resource;
    private Map<Resource, List<Resource>> resourcesMap;

    public RenderTreeNode(Resource resource, Map<Resource, List<Resource>> resourcesMap) {
        this.resource = resource;
        this.resourcesMap = resourcesMap;
    }

    @GraphQLField
    public GqlJcrNode getNode() {
        try {
            return SpecializedTypesHandler.getNode(resource.getNode());
        } catch (RepositoryException e) {
            throw new BaseGqlClientException(e, ErrorType.DataFetchingException);
        }
    }

    @GraphQLField
    public List<RenderTreeNode> getChildren() {
        return resourcesMap.get(resource).stream().map((r) -> new RenderTreeNode(r, resourcesMap)).collect(Collectors.toList());
    }


    @GraphQLField
    public String getContextConfiguration() {
        return resource.getContextConfiguration();
    }

    @GraphQLField
    public String getTemplate() {
        return resource.getTemplate();
    }

}
