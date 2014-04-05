/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package temp;

import java.util.Timer;
import java.util.TimerTask;

public class Reminder {
    Timer timer;
    application  app;

    public Reminder(int seconds) {
    	
    	app = new application();
        app.start();
        timer = new Timer(true);
        timer.schedule(new RemindTask(), seconds*1000);
        
        
	}

    class RemindTask extends TimerTask {
        public void run() {
            System.out.format("Time's up!%n");
            
            System.out.println(app.isAlive());
            app.stopIT();
            System.out.println(app.isAlive());
            //timer.cancel();
        }
    }
    class application extends Thread  {
    	volatile boolean stop=false; 
        public void run() {
            
            int counter=0;
            while (!stop) 
            {
				
			}
        }
        
        public void stopIT()
        {
        	stop=true;
        }
        
    }

    public static void main(String args[]) {
        new Reminder(7);
        System.out.format("Task scheduled.");
    }
}