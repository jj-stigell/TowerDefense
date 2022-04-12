package TowerGame.FileIO

import TowerGame.Config.Settings
import TowerGame.Controllers.WaveController
import TowerGame.Enemies.Enemy
import TowerGame.FileIO.Reader._
import TowerGame.Helpers.{Updater, Vector2D}
import TowerGame.Player.Player
import TowerGame.Towers.Tower
import TowerGame.{Area, Game}

import java.io.{BufferedReader, FileNotFoundException, FileReader, IOException}
import javax.swing.{JFileChooser, JFrame, JOptionPane}
import javax.swing.filechooser.FileNameExtensionFilter
import scala.collection.mutable.Buffer

object Loader {

  var currentMap: Int = 1
  var maxMaps: Int = Settings.defaultMaps.length

  // For loading a new map
  var loadedMap: Array[Array[Int]] = Array(Array())
  var loadedEnemies: Buffer[Enemy] = Buffer[Enemy]()
  var loadedCurrentWave: Int = 0
  var loadedMaxWaves: Int = 0
  var loadedMaxHealth: Int = 0
  var loadedCurrentHealth: Int = 0
  var loadedMoney: Int = 0
  var loadedTowers: Buffer[Tower] = Buffer[Tower]()
  var loadedTowerLocations: Array[Vector2D] = Array[Vector2D]()

  // Load map, new map from file, save game form file or next map from the default maps list in Settings.scala file.
  def loadMap(fromFile: Boolean = false) = {

    if (fromFile) {

      // Set new map, max health and starting money
      Settings.setMap(this.loadedMap)
      Settings.maxHealth = loadedMaxHealth
      Settings.startingMoney = loadedMoney
      Settings.maxWaves = this.loadedMaxWaves

      // Reset waves and area, resetArea(true) for cleaning the towers
      Updater.resetWaves()
      Updater.resetArea(true, loadedEnemies)

      // After resetting the player set current health to one loaded ffrom file
      Player.health = loadedCurrentHealth

      // Set the correct waves
      WaveController.currentWave = this.loadedCurrentWave
      WaveController.maxWaves = this.loadedMaxWaves

      // Update stats, conditions and buttons
      Updater.updateStats()
      Updater.updateConditions()
      Updater.updateButtons()

      // Add towers if any was loaded
      if (loadedTowers.nonEmpty && loadedTowers.length == loadedTowerLocations.length) {
        var i = 0
        for (tower <- loadedTowers) { tower.changeLocation(loadedTowerLocations(i)); i += 1 }
        Area.towers = this.loadedTowers
      } else {
        Area.towers = Buffer[Tower]()
        JOptionPane.showMessageDialog(null, "Something went wrong with tower placement.\nTowers resetted.")
      }

      // Start drawing the new map
      Game.refreshMap()
      // Update the enemy paths and directions to match the new map
      Area.updateAreaPathAndDirs()
      // Set current map to max maps so game ends if player finishes the loaded game.
      this.currentMap = this.maxMaps
    } else {
      if (currentMap != maxMaps) {
        Settings.setMap(Settings.defaultMaps(currentMap))
        Area.updateAreaPathAndDirs()
        Updater.resetWaves()
        Game.refreshMap()
        currentMap += 1
      }
    }
  }

  // Load game from sav-file.
  def loadGame() = {

    var mapRead: Boolean = false
    var enemiesRead: Boolean = false
    var wavesRead: Boolean = false
    var healthRead: Boolean = false
    var moneyRead: Boolean = false
    var towersRead: Boolean = false
    var locationsRead: Boolean = false

    val fileChooser = new JFileChooser
    fileChooser.setDialogTitle("Choose a sav-file with the map and settings")

    val filter = new FileNameExtensionFilter("sav file", "sav")
    fileChooser.addChoosableFileFilter(filter)
    fileChooser.setFileFilter(filter)

    val frame = new JFrame("Load")
    val response = fileChooser.showSaveDialog(frame)

    if (response == JFileChooser.APPROVE_OPTION) {

      try {
        val selectedFile = fileChooser.getSelectedFile
        val fileIn = new FileReader(selectedFile.getAbsolutePath)
        val linesIn = new BufferedReader(fileIn)

        try {
          var currentLine = linesIn.readLine()

          while (currentLine != null) {
            currentLine = currentLine.trim.toLowerCase

            currentLine = currentLine match {
              case "#" => linesIn.readLine()
              case "#map" =>
                mapRead = true
                readMap(linesIn)
              case "#enemy" =>
                enemiesRead = true
                readEnemies(linesIn)
              case "#tower" =>
                towersRead = true
                readTowers(linesIn)
              case "#towerlocation" =>
                locationsRead = true
                readLocations(linesIn)
              case "#health" =>
                healthRead = true
                readHealth(linesIn)
              case "#money" =>
                moneyRead = true
                readMoney(linesIn)
              case "#waves" =>
                wavesRead = true
                readWaves(linesIn)
              case _ => linesIn.readLine()
            }
          }
          if (mapRead && enemiesRead && wavesRead && healthRead && moneyRead) {
            this.loadMap(true)
          } else {
            JOptionPane.showMessageDialog(null, "Something went wrong when reading the sav-file.\nTo load a game, the saved file must include at least map, enemies, waves, health and money.")
          }

        } finally {
          // Close open streams
          // This will be executed if the file has been opened
          // regardless of whether or not there were any exceptions.
          fileIn.close()
          linesIn.close()
        }
      } catch {
        case notFound: FileNotFoundException =>
        // Response here to a failed file opening.
        case e: IOException =>
        // Response here to unsuccessful reading
      }
    }
  }

}
