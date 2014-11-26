package model.repositories

import model.entity._
import org.virtuslab.unicorn.LongUnicornPlay._
import org.virtuslab.unicorn.LongUnicornPlay.driver.simple._
import play.api.db.slick.Session

trait EagerCRUDRepository[
Id <: BaseId,
E <: IdEntity[Id],
ETable <: Table[E] with IdEntityTable[Id, E],
EBox <: EagerBox[E]
]
  extends CRUDRepository[Id, E, ETable] {

  def findByIdWithEager(id: Id)(implicit s: Session): Option[EBox]

  def findPaginatedEager(skip: Int = 0, take: Int = 0)(implicit s: Session): Seq[EBox]

}
