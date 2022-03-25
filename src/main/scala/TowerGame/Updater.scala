package TowerGame

object Updater {

    /**
   * Update the stats on the screen: health, money, wave.
   */
  def updateStats() = {
    Game.healthPoints.text = "Current Health: " + Player.getHealth + "/" + Settings.maxHealth
    Game.moneyInTheBank.text = "Money: " + Player.moneyIntheBank + "$"
    Game.waveNumber.text = "Current wave: " + WaveController.currentWave + "/" + Settings.maxWaves
    updateConditions()
  }

  /**
   * Update the game changing conditions
   */
  def updateConditions() = {
    Game.gameOver = !Player.isAlive
    Game.roundOver = Player.isAlive && Area.enemies.forall(!_.isAlive)
    Game.gameWon = WaveController.currentWave > Settings.maxWaves
  }

  /**
   * Reset the game area for the next wave
   */
  def resetArea() = {
    Area.numberOfEnemies = WaveController.currentWave * Settings.numberOfEnemies
    Area.tick = 0
    Area.resetEnemyBuffer()
  }

}
