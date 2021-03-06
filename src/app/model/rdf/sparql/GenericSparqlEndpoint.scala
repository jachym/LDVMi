package model.rdf.sparql


import _root_.model.rdf.Graph
import _root_.model.rdf.vocabulary.{DSPARQL, SD}
import com.hp.hpl.jena.query.{QueryExecution, QueryExecutionFactory}
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.update.{UpdateProcessor, UpdateFactory, UpdateExecutionFactory}
import org.apache.jena.atlas.web.auth.{SimpleAuthenticator}

import scala.collection.JavaConversions._

class GenericSparqlEndpoint(val endpointURL: String, val defaultGraphs: Seq[String] = List(), val namedGraphs: Seq[String] = List()) extends SparqlEndpoint {

  def queryExecutionFactory(): String => QueryExecution = { query =>
    QueryExecutionFactory.sparqlService(
      endpointURL,
      query,
      defaultGraphs++namedGraphs,
      List()
    )
  }
  def updateExecutionFactory(): String => UpdateProcessor = { query =>
    UpdateExecutionFactory.createRemote(UpdateFactory.create(query), endpointURL, new SimpleAuthenticator("dba", "dba".toCharArray))
  }

}

object GenericSparqlEndpoint {
  def apply(instanceConfiguration: Option[Graph], configuration: Option[Graph]): Option[GenericSparqlEndpoint] = {

    getEndpointUrl(Seq(instanceConfiguration, configuration)).map { endpointUrl =>

      val configs = Seq(instanceConfiguration, configuration)

      new GenericSparqlEndpoint(
        endpointUrl,
        getDefaultGraphs(configs),
        getNamedGraphs(configs)
      )
    }
  }

  def getEndpointUrl(configurations: Seq[Option[Graph]]): Option[String] = {

    configurations.filter(_.isDefined).map(_.get.jenaModel).flatMap { configurationModel =>
      val serviceStatements = configurationModel.listStatements(null, DSPARQL.service, null).toList
      serviceStatements.map { serviceStatement =>
        serviceStatement.getObject.asResource().listProperties(SD.endpoint).toList.headOption.map(_.getObject.asResource().getURI)
      }.filter(_.isDefined).map(_.get)
    }.headOption

  }

  def getDefaultGraphs(configurations: Seq[Option[Graph]]): Seq[String] = {
    getGraphs(configurations, SD.defaultGraph)
  }

  def getNamedGraphs(configurations: Seq[Option[Graph]]): Seq[String] = {
    getGraphs(configurations, SD.namedGraph)
  }

  private def getGraphs(configurations: Seq[Option[Graph]], graphUriProperty: Property): Seq[String] = {

    val namedGraphUris = configurations.filter(_.isDefined).map(_.get.jenaModel).map { configurationModel =>
      val serviceStatements = configurationModel.listStatements(null, DSPARQL.service, null).toList
      serviceStatements.map { serviceStatement =>
        val datasetStatements = serviceStatement.getObject.asResource().listProperties(SD.defaultDataset).toList
        datasetStatements.map { datasetStatement =>
          val namedGraphsStatements = datasetStatement.getObject.asResource().listProperties(graphUriProperty).toList
          namedGraphsStatements.map { namedGraphsStatement =>
            namedGraphsStatement.getProperty(SD.name).getObject.asResource().getURI
          }
        }
      }
    }

    namedGraphUris.flatten.flatten.flatten

  }
}

