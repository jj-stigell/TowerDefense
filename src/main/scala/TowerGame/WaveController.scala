package TowerGame

object WaveController {

  val maxWaves = Settings.maxWaves
  var currentWave = 0

  // New Wave
  def launchNewWave() = {
    if (currentWave <= maxWaves) {
      currentWave += 1
      Updater.resetArea()
      Updater.updateStats()
      Game.startButton.enabled = false
      Game.roundOver = false
    }
  }

  def resetWaves() = {
    Updater.resetArea()           // Reset area before setting gameOver to false
    Game.gameOver = false
    Game.roundOver = true
    this.currentWave = 0
    Player.resetPlayer()
    Updater.updateButtons()
    Updater.updateStats()
    Updater.updateConditions()
  }

}