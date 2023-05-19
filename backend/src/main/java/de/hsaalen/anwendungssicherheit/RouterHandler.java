package de.hsaalen.anwendungssicherheit;

import io.vertx.ext.web.Router;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class RouterHandler {

    public void init(@Observes Router router) {
        router.route("/create").handler(context -> context.reroute("/"));
        router.route("/add").handler(context -> context.reroute("/"));
        router.route("/konten").handler(context -> context.reroute("/"));
        router.route("/bewegungen").handler(context -> context.reroute("/"));
    }
}
