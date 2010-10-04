package edu.dhbw.andar.pingpong;

public class GameScore {
	private GameHUD gameHUD=null;
	public int computer=0;
	public int player=0;
	
	public void setGameHUD(GameHUD hud) {
		this.gameHUD=hud;
	}

	public void incComputerScore() {
		computer++;
		if(computer>3) {
			if(gameHUD!=null)
				gameHUD.playerLost();
			computer = 0;
			player = 0;
		}
	}
	
	public void incPlayerScore() {
		player++;
		if(player>3) {	
			if(gameHUD!=null)
				gameHUD.playerWon();
			computer = 0;
			player = 0;
		}
	}
}
