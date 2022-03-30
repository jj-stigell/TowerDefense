package TowerGame.Towers

import TowerGame.Enemies.Enemy
import TowerGame.{Settings, Vector2D}
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import scala.collection.mutable.Buffer
import scala.swing.{Color, Rectangle}

abstract class Tower(location: Vector2D) {

  val redBar: Color = new Color(255, 0, 0)
  val greenBar: Color = new Color(0, 255, 0)
  val coolDownTime: Int
  val towerSize: Int
  val range: Double
  val towerColor: Color
  val coolDownPerCycle: Int
  val damageGivenPerHit: Int  // To the enemy if tower shoots
  var coolDownCounter = 0

  /**
   * @return Location vector of the tower
   */
  def getLocation = this.location

  /**
   * Scan the tower proximity and if enemy is found attack one of the enemies.
   *
   * @param enemies Buffer with all enemies
   */
  def scanProximity(enemies: Buffer[Enemy]) = {
    if (this.coolDownCounter > 0) {
      this.coolDownCounter -= this.coolDownPerCycle
    } else {
      var foundEnemies = enemies.filter(enemy => enemy.isAlive && (enemy.getLocation.x - this.location.x).abs < range && (enemy.getLocation.y - this.location.y).abs < range)
      if (foundEnemies.nonEmpty){
        this.shoot(foundEnemies.head)
      }
    }
  }

  /**
   * Shoot one close by enemy.
   *
   * @param enemy Enemy to be shot
   */
  def shoot(enemy: Enemy) = {
    // shoot near by enemy, search enemies on proximity
    enemy.getHitByTower(this.damageGivenPerHit)
    this.coolDownCounter = this.coolDownTime
  }

  /**
   * Draw the tower and its cool down meter.
   *
   * @param g Graphics2D
   */
  def draw(g: Graphics2D) = {
    val circle = new Ellipse2D.Double(10, 10, this.towerSize, this.towerSize)
    g.setColor(this.towerColor)
    val oldTransform = g.getTransform
    g.translate(this.location.x, this.location.y)
    g.fill(circle)
    g.setTransform(oldTransform)

    // Cool down meter indicating when tower can shoot again
    var coolMeter = new Rectangle(45, 10, this.coolDownTime, Settings.height / 100)
    g.setColor(this.greenBar)
    g.translate(this.location.x, this.location.y)
    g.fill(coolMeter)
    g.setTransform(oldTransform)

    coolMeter = new Rectangle(45, 10, this.coolDownCounter, Settings.height / 100)
    g.setColor(this.redBar)
    g.translate(this.location.x, this.location.y)
    g.fill(coolMeter)
    g.setTransform(oldTransform)
  }

}