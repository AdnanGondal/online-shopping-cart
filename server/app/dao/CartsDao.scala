package dao

import com.example.onlineshoppingcart.shared.{Cart, CartKey, ProductInCart}
import com.sun.org.apache.xpath.internal.operations.Bool
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CartsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                         cc: ControllerComponents
                        )(implicit ec: ExecutionContext)
  extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  private class CartsTable(tag: Tag) extends Table[Cart](tag, "CART") {
    def user = column[String]("USER")
    def productCode = column[String]("CODE")
    def quantity = column[Int]("QTY")
    override def * =
      (user, productCode, quantity) <> (Cart.tupled, Cart.unapply)
  }
  private val carts = TableQuery[CartsTable]

  def all(): Future[Seq[Cart]] = db.run(carts.result)

  def cart4(usr: String): Future[Seq[Cart]] = db.run(carts.filter(_.user === usr).result)

  def insert(cart: Cart): Future[_] = db.run(carts += cart)

  private def matchKey(c: CartsTable, cart: CartKey): Rep[Boolean] = {
    c.user === cart.user && c.productCode === cart.productCode
  }

  def remove(cart: ProductInCart): Future[Int] = {
    db.run(carts.filter(c => matchKey(c, cart)).delete)
  }

  def update(cart: Cart): Future[Int] = {
    val q = for {
      c <- carts if matchKey(c, cart)
    } yield c.quantity
    // get data
                    // enter new value
    db.run(q.update(cart.quantity))
  }

}
