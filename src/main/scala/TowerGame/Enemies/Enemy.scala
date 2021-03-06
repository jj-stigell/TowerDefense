package TowerGame.Enemies

import TowerGame.Helpers.{Updater, Vector2D}
import TowerGame.Player.Player

import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import scala.collection.mutable.Buffer
import scala.swing.Color

/**
 * Enemy is a ball that moves on the predetermined path and tries to reach the players tower.
 * @param enemyPath       Full path the enemy moves
 * @param directionSet    All the directions enemy will move on the map, must be multiplied with the enemy speed
 */
abstract class Enemy(enemyPath: Buffer[Vector2D], directionSet: Buffer[(Int, Int)]) {

  val speed: Double
  val enemyType: String
  val rewardFromDestroying: Int
  val damageGivenPerHit: Int
  val enemyColor: Color
  var health: Int
  val enemySize: Int
  var enemyDirections: Buffer[Vector2D] = directionSet.map(x => Vector2D(x._1 * this.speed, x._2 * this.speed))

  var location: Vector2D = enemyPath.head                                               // Initial location of the enemy
  var directionVector: Vector2D = enemyDirections.head                                  // Initial direction (head of directions) set as the speed
  var alive: Boolean = true
  var launched: Boolean = false
  var playerHit: Boolean = false
  var turningPoint: Vector2D = enemyPath(1)                                             // Start with the first turning point
  var path: Buffer[Vector2D] = enemyPath.drop(2)                                        // Drop first 2, because first is set as the first location and second one as the first turning point
  var directions: Buffer[Vector2D] = enemyDirections.drop(1)                            // Drop first, already as the initial direction

  /** Recalculate the correct directios after updating map, scaled for the enemy speed */
  def reCalcDirections() = {
    enemyDirections = directionSet.map(x => Vector2D(x._1 * this.speed, x._2 * this.speed))
    directionVector = enemyDirections.head
    directions = enemyDirections.drop(1)
  }

  /** Check if the enemy is still alive or destroyed. */
  def isAlive: Boolean = this.alive

  /** Only launched enemies move on the map. */
  def launchEnemy(): Unit = this.launched = true

  /**
   * For the tower to check the location of the enemy.
   * @return Location Vector2D
   */
  def getLocation: Vector2D = this.location

  /** Player can attack the enemy when close by, kills the enemy if health goes zero. */
  def getHitByTower(dmg: Int): Unit = {
    if (this.health - dmg > 0) {
      this.health -= dmg
    } else {
      this.alive = false
      Player.addMoney(this.rewardFromDestroying)
      Updater.updateStats()
      Updater.updateButtons()
      Updater.updateConditions()
    }
  }

  /**
   * Move the enemy if alive to the direction of current movement.
   * If enemy reaches player tower, reduce player health and update stats.
   */
  def move() = {

    if (isAlive) {
      if (this.closeToTurningPoint) {
        if (this.path.isEmpty && !this.playerHit) {
          Player.getHitByEnemy(this)
          this.playerHit = true
          this.alive = false
          Updater.updateStats()
          Updater.updateConditions()
        } else {
          this.directionVector = this.directions.head
          this.directions = this.directions.drop(1)
          this.turningPoint = this.path.head
          this.path = this.path.drop(1)
        }
      } else {
        this.location = this.location + this.directionVector
      }
    }
  }

  /**
   * Check if the enemy is close to the turning point. Compare enemy location and turningpoint location.
   * Takes into account the enemy speed and difference it creates to the compared values.
   * @return Boolean true if the enemy is close to turning point, otherwise false
   */
  def closeToTurningPoint: Boolean = (this.location.x - this.turningPoint.x).abs < this.speed && (this.location.y - turningPoint.y).abs < this.speed

  /**
   * Draw the enemy on the map.
   * @param g Graphics2D
   */
  def draw(g: Graphics2D) = {
    if (this.isAlive) {
      val circle = new Ellipse2D.Double(10, 10, this.enemySize, this.enemySize)
      g.setColor(this.enemyColor)
      val oldTransform = g.getTransform
      g.translate(this.location.x, this.location.y)
      g.fill(circle)
      g.setTransform(oldTransform)
    }
  }

}
