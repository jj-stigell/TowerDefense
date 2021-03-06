package TowerGame

import TowerGame.Config.Settings
import TowerGame.Controllers.WaveController
import TowerGame.FileIO.{Loader, Saver}
import TowerGame.Helpers.{Updater, Vector2D}
import TowerGame.Towers.{BigTower, SmallTower}

import java.awt.event.ActionListener
import java.awt.{Color, Graphics2D, RenderingHints}
import javax.swing.JOptionPane
import scala.swing._
import scala.swing.event.{ButtonClicked, MouseMoved}

/** Game object for drawing the GUI and buttons for the game window. */
object Game extends SimpleSwingApplication {

  // Window parameters and game map are loaded from settings
  val width: Int = Settings.width
  val height: Int = Settings.height
  val fullHeight: Int = Settings.fullHeight
  var map: Array[Array[Int]] = Settings.map
  var blockWidth: Int = Settings.blockLengthX
  var blockHeight: Int = Settings.blockLengthY

  var gameOver: Boolean = false
  var mapWon: Boolean = false
  var gameWon: Boolean = false
  var roundOver: Boolean = true
  var towerBuying: Boolean = false
  var blocked: Boolean = false

  // Buttons
  val startButton: Button = new Button("Start new wave!")
  val loadGameButton: Button = new Button("Load Game")
  val saveGameButton: Button = new Button("Save Game")
  val quitGameButton: Button = new Button("Quit")
  val buySmallTower: Button = new Button(s"Buy Small Tower: ${Settings.smallTowerPrice}€")
  val buyBigTower: Button = new Button(s"Buy Big Tower: ${Settings.bigTowerPrice}€")
  val restartMap: Button = new Button("Restart Game")
  val nextMap: Button = new Button("Next Map")
  restartMap.visible = false
  nextMap.visible = false

  // Labels
  val healthPoints: Label = new Label
  healthPoints.text = s"Current Health: ${Player.Player.getHealth}/${Settings.maxHealth}"
  val moneyInTheBank: Label = new Label
  moneyInTheBank.text = s"Money: ${Player.Player.moneyIntheBank}€"
  val waveNumber: Label = new Label
  waveNumber.text = s"Current wave: ${WaveController.currentWave}/${WaveController.maxWaves}"

  /** Refresh map after setting new map. */
  def refreshMap() = {
    this.map = Settings.map
    this.blockWidth = Settings.blockLengthX
    this.blockHeight = Settings.blockLengthY
  }

  /** MainFrame is the application window class in scala-swing. */
  def top = new MainFrame {

    title = Settings.title
    resizable = Settings.resizable
    minimumSize = new Dimension(width, fullHeight)
    preferredSize = new Dimension(width, fullHeight)
    maximumSize = new Dimension(width, fullHeight)

    /** The panel inside our window, where the enemies are moving. */
    val arena = new Panel {

      /**
       * Standard panel is just a dull box where we can add other components.
       * Overriding the paintComponent method enables drawing own graphics.  It gets as a parameter
       * a Graphics2D object, through which it is possible to draw into the object,
       * change colors, coordinates, line thickness etc.
       */
      override def paintComponent(g: Graphics2D) = {

        if (Game.gameOver || Game.gameWon || Game.mapWon) {
          g.setColor(new Color(0, 0, 0))
          g.fillRect(0, 0, width, height)
          g.setColor(new Color(255, 255, 255))
          if (Game.gameOver) g.drawString("GAME OVER, RESTART FROM MENU!", width / 2, height / 2)
          else if (Game.gameWon) g.drawString("YOU WIN THE GAME CONGRATS!", width / 2, height / 2)
          else g.drawString("YOU WIN THIS MAP! ADVANCE TO NEXT MAP FROM MENU!", width / 2, height / 2)
        } else {

          g.setColor(new Color(51, 153, 51))
          g.fillRect(0, 0, width, fullHeight)
          g.setColor(new Color(198, 140, 83))
          var rowNumber = 0

          // Draw the map with path and tower placing area
          for (row <- map) {
            var columnNumber = 0
            for (element <- row) {
              if (element == 1 || element == 2) {
                val x0 = columnNumber * blockWidth
                val y0 = rowNumber * blockHeight
                g.fillRect(x0, y0, blockWidth, blockHeight)
              } else if (element == 3) {
                g.setColor(new Color(87, 83, 198))
                val x0 = columnNumber * blockWidth
                val y0 = rowNumber * blockHeight
                g.fillRect(x0, y0, blockWidth, blockHeight)
                g.setColor(new Color(198, 140, 83))
              }
              columnNumber += 1
            }
            rowNumber += 1
          }

          // Ask Graphics2D to provide smoother graphics
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

          // Draw the area
          Area.draw(g)
        }
      }
    }

    // Panels
    val verticalPanel: BoxPanel = new BoxPanel(Orientation.Vertical)
    val controlPanel: BoxPanel = new BoxPanel(Orientation.Horizontal)

    // Add buttons and labels to panels
    controlPanel.contents += Game.saveGameButton
    controlPanel.contents += Game.loadGameButton
    controlPanel.contents += Game.nextMap
    controlPanel.contents += Game.restartMap
    controlPanel.contents += Game.startButton
    controlPanel.contents += Game.buySmallTower
    controlPanel.contents += Game.buyBigTower
    controlPanel.contents += Game.healthPoints
    controlPanel.contents += Game.moneyInTheBank
    controlPanel.contents += Game.waveNumber
    controlPanel.contents += Game.quitGameButton
    verticalPanel.contents += this.arena
    verticalPanel.contents += this.controlPanel
    verticalPanel.contents -= this.controlPanel
    verticalPanel.contents += this.controlPanel
    this.contents = verticalPanel

    // Ask the object to listen mouse and button events in our arena panel
    listenTo(arena.mouse.clicks)
    listenTo(arena.mouse.moves)
    listenTo(Game.startButton)
    listenTo(Game.buySmallTower)
    listenTo(Game.buyBigTower)
    listenTo(Game.loadGameButton)
    listenTo(Game.saveGameButton)
    listenTo(Game.restartMap)
    listenTo(Game.nextMap)
    listenTo(Game.quitGameButton)

    // Respond to events based on input
    this.reactions += {
      case scala.swing.event.MousePressed(src, point, _, _, _) => if (Game.towerBuying && !Game.blocked) Area.placeTower(point.x, point.y)
      case locationMouse: MouseMoved => if (Game.towerBuying) Area.newTowerLocation(locationMouse)
      case clickEvent: ButtonClicked => {
        clickEvent.source match {
          case Game.startButton => WaveController.launchNewWave()
          case Game.buySmallTower =>
            Game.towerBuying = true
            Area.newTower = new SmallTower(Vector2D(0, 0))
          case Game.buyBigTower =>
            Game.towerBuying = true
            Area.newTower = new BigTower(Vector2D(0, 0))
          case Game.loadGameButton => Loader.loadFromFile()
          case Game.saveGameButton => Saver.saveGame()
          case Game.restartMap => Updater.resetWaves()
          case Game.nextMap => Loader.loadMap()
          case Game.quitGameButton => if (JOptionPane.showConfirmDialog(null, "Exit Program?\nRemember to save game!", "EXIT", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0)
          case _ =>
        }
      }
    }

    /**
     * This event listener and swing timer allow periodic repetitive
     * activity in the event listening thread. The game is light enough
     * to be drawn in the thread without additional buffers or threads.
     */
    val listener = new ActionListener() {
      def actionPerformed(e: java.awt.event.ActionEvent) = {
        if (Game.gameOver || Game.mapWon || Game.roundOver) {
          Updater.updateButtons()
          arena.repaint()
        } else {
          Area.step()
          arena.repaint()
        }
      }
    }

    /**
     * Timer sends ActionEvent to ActionListener, interval set in Settings,
     * when the space moves forward and the screen is redrawn.
     */
    val timer = new javax.swing.Timer(Settings.interval, listener)
    timer.start()
  }

}
