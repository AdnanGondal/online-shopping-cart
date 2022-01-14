package dao
import com.example.onlineshoppingcart.shared.Product
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProductsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                            cc: ControllerComponents
                           )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile]{

import profile.api._



  private class ProductsTable(tag: Tag) extends Table[Product](tag,
    "PRODUCT") {
    def name = column[String]("NAME")
    def code = column[String]("CODE")
    def description = column[String]("DESCRIPTION")
    def price = column[Double]("PRICE")
    override def * = (name, code, description, price) <>
      (Product.tupled, Product.unapply)
  }

  private val products = TableQuery[ProductsTable]

  def all(): Future[Seq[Product]] = db.run(products.result)

  def insert(product: Product): Future[Unit] = db.run(products
    insertOrUpdate product).map { _ => () }
}
