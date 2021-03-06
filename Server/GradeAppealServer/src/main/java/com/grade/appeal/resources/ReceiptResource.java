package com.grade.appeal.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.grade.appeal.activities.CompleteOrderActivity;
import com.grade.appeal.activities.NoSuchOrderException;
import com.grade.appeal.activities.OrderAlreadyCompletedException;
import com.grade.appeal.activities.OrderNotPaidException;
import com.grade.appeal.activities.ReadReceiptActivity;
import com.grade.appeal.model.Identifier;
import com.grade.appeal.representations.OrderRepresentation;
import com.grade.appeal.representations.ReceiptRepresentation;
import com.grade.appeal.representations.RestbucksUri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/receipt")
public class ReceiptResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptResource.class);

    private @Context
    UriInfo uriInfo;

    public ReceiptResource() {
        LOG.info("Receipt Resource constructor");
    }

    /**
     * Used in test cases only to allow the injection of a mock UriInfo.
     * 
     * @param uriInfo
     */
    public ReceiptResource(UriInfo uriInfo) {
        this.uriInfo = uriInfo;

    }

    @GET
    @Path("/{orderId}")
    @Produces("application/vnd.restbucks+xml")
    public Response getReceipt() {
        LOG.info("Retrieving a  Receipt Resource");
        
        Response response;
        
        try {
            ReceiptRepresentation responseRepresentation = new ReadReceiptActivity().read(new RestbucksUri(uriInfo.getRequestUri()));
            response = Response.ok().entity(responseRepresentation).build();
        } catch (OrderAlreadyCompletedException oce) {
            LOG.debug("Order already completed");
            response = Response.status(Status.NO_CONTENT).build();
        } catch (OrderNotPaidException onpe) {
            LOG.debug("Order not paid");
            response = Response.status(Status.NOT_FOUND).build();
        } catch (NoSuchOrderException nsoe) {
            LOG.debug("No such order");
            response = Response.status(Status.NOT_FOUND).build();
        }
        
        LOG.debug("The responce for the retrieve receipt request is {}", response);
        
        return response;
    }
    
    @DELETE
    @Path("/{orderId}")
    public Response completeOrder(@PathParam("orderId")String identifier) {
        LOG.info("Retrieving a  Receipt Resource");
        
        Response response;
        
        try {
            OrderRepresentation finalizedOrderRepresentation = new CompleteOrderActivity().completeOrder(new Identifier(identifier));
            response = Response.ok().entity(finalizedOrderRepresentation).build();
        } catch (OrderAlreadyCompletedException oce) {
            LOG.debug("Order already completed");
            response = Response.status(Status.NO_CONTENT).build();
        } catch (NoSuchOrderException nsoe) {
            LOG.debug("No such order");
            response = Response.status(Status.NOT_FOUND).build();
        } catch (OrderNotPaidException onpe) {
            LOG.debug("Order not paid ");
            response = Response.status(Status.CONFLICT).build();
        }
        
        LOG.debug("The response for the delete receipt request is {}", response);
        
        return response;
    }
}
