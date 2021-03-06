package model.rdf.sparql.geo.query

import model.rdf.sparql.geo.MapQueryData
import model.rdf.sparql.query.SparqlQuery
import model.rdf.sparql.{ValueFilter, VariableGenerator}
import model.rdf.vocabulary.SKOS


class WKTEntitiesQuery(queryData: MapQueryData) extends SparqlQuery {

  lazy val variableGenerator = new VariableGenerator

  def get: String = {
    val q = prefixes +
      """
        | SELECT ?s ?l ?p %v WHERE {
        |   ?s <http://www.opengis.net/ont/geosparql#hasGeometry> ?g ;
        |      rdfs:label ?l .
        |   ?g <http://www.opengis.net/ont/geosparql#asWKT> ?p .
        |
        |   %r
        | } LIMIT 1
      """
        .replaceAll(
          "%r", getRestrictions(queryData.filters))
        .replaceAll("%v", if (queryData.filters.nonEmpty) { "?v1" } else {"" })
        .stripMargin
    println(q)
    q
  }

  private def prefixes =
    """
      | PREFIX skos: <%skos>
      | PREFIX rdfs: <%rdfs>
      |

    """
      .replaceAll("%skos", SKOS.PREFIX_URL)
      .replaceAll("%rdfs", "http://www.w3.org/2000/01/rdf-schema#")
      .stripMargin

  private def getRestrictions(rule: Map[String, Seq[ValueFilter]]): String = {
    rule.map { case (uri, valueFilters) =>
      """
        |  ?s <%s> %v
        |  %rf
      """
        .replaceAll("%s", uri)
        .replaceAll("%v", variableGenerator.next.getVariable)
        .replaceAll("%rf", restrictionFilters(variableGenerator.getVariable, valueFilters))
        .stripMargin
    }.mkString("\n")
  }

  private def restrictionFilters(variable: String, filters: Seq[ValueFilter]): String = {
    filters.filterNot(_.isActive.getOrElse(false)).map { f =>
      labelOrUri(f).map { s =>
        """
          |  FILTER(%v != %fv)
        """
          .replaceAll("%v", variable)
          .replaceAll("%fv", s)
          .stripMargin
      }
    }.filter(_.isDefined).map(_.get).mkString("\n")
  }

  private def labelOrUri(f: ValueFilter) = {
    f match {
      case u if f.uri.isDefined => u.uri.map("<" + _ + ">")
      case l if f.label.isDefined => l.label.map("'" + _ + "'")
      case _ => None
    }
  }

}

object WKTEntitiesQuery {

  object NodeVariables extends Enumeration {
    type NodeVariables = Value
    val geolocatedEntity = Value("s")
    val wkt = Value("p")
    val title = Value("l")
  }

}
