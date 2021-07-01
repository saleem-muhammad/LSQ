package org.aksw.simba.lsq.spark.cmd.impl;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.io.json.RDFNodeJsonUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import scala.Tuple2;

public class RddOfTripleOps {

    public static <K> JavaPairRDD<K, Model> groupTriplesIntoModels(
            JavaPairRDD<K, Triple> rdd) {
        return rdd.combineByKey(
                triple -> ModelFactory.createDefaultModel(),
                (model, triple) -> { model.getGraph().add(triple); return model; },
                (m1, m2) -> { m1.add(m2); return m1; });
    }

    public static <K> JavaPairRDD<K, Model> groupBy(
            JavaRDD<Triple> rdd,
            SerializableFunction<? super Triple, K> tripleToKey) {

        JavaPairRDD<K, Triple> tmp = rdd.mapToPair(t -> new Tuple2<K, Triple>(tripleToKey.apply(t), t));

        return groupTriplesIntoModels(tmp);
    }


    public static JavaPairRDD<Node, Model> groupBySubjectNodes(JavaRDD<Triple> rdd) {
        return groupBy(rdd, Triple::getSubject);
    }

    public static JavaPairRDD<Node, Model> groupByObjectNodes(JavaRDD<Triple> rdd) {
        return groupBy(rdd, Triple::getObject);
    }

    /** This function mainly exists for completeness */
    public static JavaPairRDD<Node, Model> groupByPredicateNodes(JavaRDD<Triple> rdd) {
        return groupBy(rdd, Triple::getPredicate);
    }


    public static JavaPairRDD<String, Model> groupBySubjects(JavaRDD<Triple> rdd) {
        return groupBy(rdd, t -> toGraphName(t.getSubject()));
    }

    public static JavaPairRDD<String, Model> groupByObjects(JavaRDD<Triple> rdd) {
        return groupBy(rdd, t -> toGraphName(t.getObject()));
    }

    /** This function mainly exists for completeness */
    public static JavaPairRDD<String, Model> groupByPredicates(JavaRDD<Triple> rdd) {
        return groupBy(rdd, t -> toGraphName(t.getPredicate()));
    }


    /**
     * Map a node losslessly to an IRI suitable for use as a graph name
     * This is needed to e.g. group triples by objects into named graphs in order
     * to use all the RDF machinery - named graphs need to be IRIs.
     *
     * This mapping is used in the 'RddOfDatasetOps.naturalResource'
     *
     * TODO Add a reverse mapping */
    public static String toGraphName(Node node) {
        if (node.isURI()) {
            return node.getURI();
        } else if (node.isBlank()) {
            return "_:" + node.getBlankNodeLabel();
        } else if (node.isNodeTriple()) {
            return "x-rdfstar:" + StringUtils.urlEncode(RDFNodeJsonUtils.nodeToStr(node));
        } else if (node.isLiteral()) {
            return "x-literal:" + StringUtils.urlEncode(RDFNodeJsonUtils.nodeToStr(node));
        } else {
            throw new RuntimeException("Unknown node: " + node);
        }
    }


}
