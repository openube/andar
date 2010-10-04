package edu.dhbw.andar.pingpong;

import javax.microedition.khronos.opengles.GL10;

public class ComputerPaddle extends Paddle {
	
	private final double speed = 0.00000006;
	private final double speed_slow = 0.00000004;
	private final float epsilon = 0.1f;
	
	private Ball ball;
	
	public ComputerPaddle(int ID, GameCenter center, Ball ball) {
		super(ID, center);
		this.ball = ball;
	}
	
	@Override
	public void update(long time) {
		boolean act = false;
		switch(ID) {
		case 0:
			if(ball.getX() > 0) act = true;
			break;
		case 1:
			if(ball.getX() < 0) act = true;
			break;
		}
		if(act) {
			float ballY = ball.getY();
			oy = y;
			if(ballY<y) {
				//move up
				this.y -= time * speed;
			} else if (ballY<y+width) {
				//slowy adjust to center
				float centerY = y + width / 2.0f; 
				if(ballY < centerY) {
					this.y -= time * speed_slow;
				} else {
					this.y += time * speed_slow;
				}
			} else {
				//move down
				this.y += time * speed;
			}
			//check boundaries
			checkBoundaries();
		} else {
			//move to the middle
			float centerY = y + width / 2.0f;
			if((centerY < 0 + epsilon) && (centerY > 0 - epsilon)) {
			} else if ( centerY > 0 ) {
				this.y -= time * speed_slow;
			} else if ( centerY < 0 ) {
				this.y += time * speed_slow;
			}
		}
	}

	@Override
	public void init(GL10 gl) {
	}
	
}
