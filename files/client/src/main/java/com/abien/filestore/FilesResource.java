package com.abien.filestore;

import java.net.URI;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.connectorz.files.Bucket;
import org.connectorz.files.BucketStore;

/**
 *
 * @author adam bien, adam-bien.com
 */
@Path("files")
@Stateless
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class FilesResource {

    @Resource(name = "jca/files")
    BucketStore bucketStore;

    @PUT
    @Path("{id}")
    public Response put(@PathParam("id") String id, String content) {
        try (Bucket bucket = bucketStore.getBucket();) {
            bucket.write(id, content.getBytes());
        }
        URI createdURI = URI.create(id);
        return Response.created(createdURI).build();
    }

    @GET
    @Path("{id}")
    public String fetch(@PathParam("id") String id) {
        try (Bucket bucket = bucketStore.getBucket();) {
            final byte[] content = bucket.fetch(id);
            if(content == null)
                return null;
            return new String(content);
        }
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") String id) {
        try (Bucket bucket = bucketStore.getBucket();) {
            bucket.delete(id);
        }
    }
}
