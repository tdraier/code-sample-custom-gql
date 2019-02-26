package org.jahia.modules.customGql;

import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeImpl;
import org.jahia.modules.graphql.provider.dxm.node.SpecializedType;
import org.jahia.services.content.JCRNodeWrapper;

@SpecializedType("bootstrap3nt:columns")
public class Column extends GqlJcrNodeImpl implements GqlJcrNode {
    public Column(JCRNodeWrapper node) {
        super(node);
    }

    public Column(JCRNodeWrapper node, String type) {
        super(node, type);
    }

    @GraphQLField
    public String getTest() {
        return "test";
    }
}
