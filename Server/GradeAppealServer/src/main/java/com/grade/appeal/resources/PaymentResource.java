package com.grade.appeal.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.grade.appeal.activities.InvalidPaymentException;
import com.grade.appeal.activities.NoSuchOrderException;
import com.grade.appeal.activities.PaymentActivity;
import com.grade.appeal.activities.UpdateException;
import com.grade.appeal.model.Identifier;
import com.grade.appeal.representations.Link;
import com.grade.appeal.representations.PaymentRepresentation;
import com.grade.appeal.representations.Representation;
import com.grade.appeal.representations.RestbucksUri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/payment/{paymentId}")
public class PaymentResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(PaymentResource.class);
    
    private @Context UriInfo uriInfo;
    
    public PaymentResource(){
        LOG.info("Payment Resource Constructor");
    }
    
    /**
     * Used in test cases only to allow the injection of a mock UriInfo.
     * @param uriInfo
     */
    public PaymentResource(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @PUT
    @Consumes("application/vnd.restbucks+xml")
    @Produces("application/vnd.restbucks+xml")
    public Response pay(PaymentRepresentation paymentRepresentation) {
        LOG.info("Making a new payment");
        
        Response response;
        
        try {
            response = Response.created(uriInfo.getRequestUri()).entity(
                    new PaymentActivity().pay(paymentRepresentation.getPayment(), 
                            new RestbucksUri(uriInfo.getRequestUri()))).build();
        } catch(NoSuchOrderException nsoe) {
            LOG.debug("No order for payment {}", paymentRepresentation);
            response = Response.status(Status.NOT_FOUND).build();
        } catch(UpdateException ue) {
            LOG.debug("Invalid update to payment {}", paymentRepresentation);
            Identifier identifier = new RestbucksUri(uriInfo.getRequestUri()).getId();
            Link link = new Link(Representation.SELF_REL_VALUE, new RestbucksUri(uriInfo.getBaseUri().toString() + "order/" + identifier));
            response = Response.status(Status.FORBIDDEN).entity(link).build();
        } catch(InvalidPaymentException ipe) {
            LOG.debug("Invalid Payment for Order");
            response = Response.status(Status.BAD_REQUEST).build();
        } catch(Exception e) {
            LOG.debug("Someting when wrong with processing payment");
            response = Response.serverError().build();
        }
        
        LOG.debug("Created the new Payment activity {}", response);
        
        return response;
    }
}
