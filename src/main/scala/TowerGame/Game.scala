package TowerGame

import TowerGame.FileIO.{GameLoader, GameSaver}
import java.awt.event.ActionListener
import java.awt.{Color, Graphics2D, RenderingHints}
import javax.swing.JOptionPane
import scala.swing._
import scala.swing.event.{ButtonClicked, MouseMoved}

object Game extends SimpleSwingApplication {

  val width = Settings.width
  val height = Settings.height
  val fullHeight = Settings.fullHeight
  var map = Settings.map
  var blockWidth = Settings.blockLengthX
  var blockHeight = Settings.blockLengthY

  var gameOver = false
  var mapWon = false
  var gameWon = false
  var roundOver = true
  var towerBuying = false
  var blocked = false

  // Buttons and Labels
  val startButton = new Button("Start new wave!")
  val menuButton = new Button("Menu")
  val loadGameButton = new Button("Load Game")
  val saveGameButton = new Button("Save Game")
  val quitGameButton = new Button("Quit")
  val buyTowerButton = new Button("Buy Tower: " + Settings.towerPrice + "$")

  val restartMap = new Button("Restart Game")
  restartMap.visible = false
  val nextMap = new Button("Next Map")
  nextMap.visible = false

  val healthPoints = new Label
  healthPoints.text = "Current Health: " + Player.getHealth + "/" + Settings.maxHealth
  val moneyInTheBank = new Label
  moneyInTheBank.text = "Money: " + Player.moneyIntheBank + "$"
  val waveNumber = new Label
  waveNumber.text = "Current wave: " + WaveController.currentWave + "/" + Settings.maxWaves

  def refreshMap() = {
    this.map = Settings.map
    this.blockWidth = Settings.blockLengthX
    this.blockHeight = Settings.blockLengthY
  }


  /**
   * MainFrame is the application window class in scala-swing.
   */
  def top = new MainFrame {

    title = Settings.title
    resizable = Settings.resizable
    minimumSize = new Dimension(width, fullHeight)
    preferredSize = new Dimension(width, fullHeight)
    maximumSize = new Dimension(width, fullHeight)

    /**
     * The panel inside our window, where the enemies are moving.
     */
    val arena = new Panel {

      /**
       * Standard panel is just a dull box where we can add other components.
       *
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

          // Draw path and tower at the end
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

          // Ask Graphics2D to provide us smoother graphics, i.e., antialising
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

          // Draw the area
          Area.draw(g)
        }
      }
    }

    // Panels
    val verticalPanel = new BoxPanel(Orientation.Vertical)
    val controlPanel = new BoxPanel(Orientation.Horizontal)

    // Add buttons and labels to panels
    controlPanel.contents += menuButton
    controlPanel.contents += saveGameButton
    controlPanel.contents += loadGameButton
    controlPanel.contents += nextMap
    controlPanel.contents += restartMap
    controlPanel.contents += startButton
    controlPanel.contents += buyTowerButton
    controlPanel.contents += healthPoints
    controlPanel.contents += moneyInTheBank
    controlPanel.contents += waveNumber
    controlPanel.contents += quitGameButton
    verticalPanel.contents += arena
    verticalPanel.contents += controlPanel
    verticalPanel.contents -= controlPanel
    verticalPanel.contents += controlPanel
    contents = verticalPanel

    // Ask the object to listed mouse events in our arena panel
    listenTo(arena.mouse.clicks)
    listenTo(arena.mouse.moves)
    listenTo(startButton)
    listenTo(menuButton)
    listenTo(buyTowerButton)
    listenTo(loadGameButton)
    listenTo(saveGameButton)
    listenTo(restartMap)
    listenTo(nextMap)
    listenTo(quitGameButton)

    // And now that the class responds to events
    this.reactions += {
      case scala.swing.event.MousePressed(src, point, _, _, _) => if (towerBuying && !blocked) Area.placeTower(point.x, point.y)
      case locationMouse: MouseMoved => if (towerBuying) Area.newTowerLocation(locationMouse)
      case clickEvent: ButtonClicked => {
        clickEvent.source match {
          case Game.menuButton => println("klikattu menua nappie")
          case Game.startButton => WaveController.launchNewWave()
          case Game.buyTowerButton => towerBuying = true
          case Game.loadGameButton => GameLoader.loadGame()
          case Game.saveGameButton => GameSaver.saveGame()
          case Game.restartMap => Updater.resetWaves()
          case Game.nextMap => GameLoader.loadMap()
          case Game.quitGameButton => if (JOptionPane.showConfirmDialog(null, "Exit Program?\nRemember to save game!", "EXIT", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0)
          case _ =>
        }
      }
    }

    // This event listener and swing timer allow periodic repetitive
    // activity in the event listening thread. The game is light enough
    // to be drawn in the thread without additional buffers or threads.
    val listener = new ActionListener() {
      def actionPerformed(e: java.awt.event.ActionEvent) = {
        if (Game.gameOver) {
          Updater.updateButtons()
          arena.repaint()
        } else if (Game.mapWon) {
          Updater.updateButtons()
          arena.repaint()
        } else if (Game.roundOver) {
          Updater.updateButtons()
          arena.repaint()
        } else {
          Area.step()
          arena.repaint()
        }
      }
    }

    // Timer sends ActionEvent to ActionListener every 6ms,
    // when the space moves forward and the screen is redrawn.
    // This code therefore allows animation
    val timer = new javax.swing.Timer(Settings.interval, listener)
    timer.start()

  }

}
