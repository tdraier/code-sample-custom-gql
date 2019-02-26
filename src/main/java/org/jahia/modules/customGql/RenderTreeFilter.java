package org.jahia.modules.customGql;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(service = RenderFilter.class)
public class RenderTreeFilter extends AbstractFilter {

    public RenderTreeFilter() {
        addCondition((renderContext, resource) -> renderContext.getRequest().getAttribute("gqlRenderChildren") != null);
        setPriority(20.f);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
//        if (!resource.getNode().getPath().startsWith("/modules")) {
            Map<Resource, List<Resource>> resourcesMap = (Map<Resource, List<Resource>>) renderContext.getRequest().getAttribute("gqlRenderChildren");
            Resource parentResource = (Resource) renderContext.getRequest().getAttribute("parentResource");

            if (parentResource == null || !parentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                List<Resource> children = resourcesMap.get(parentResource);
                children.add(resource);
                resourcesMap.put(resource, new ArrayList<>());
                chain.pushAttribute(renderContext.getRequest(), "parentResource", resource);
            }
//        }
        return super.prepare(renderContext, resource, chain);
    }

}
