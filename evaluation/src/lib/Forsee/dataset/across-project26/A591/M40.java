		protected void animateVertically( int posx, int fromY, int toY ) throws InterruptedException
		{

			toaster.setLocation( posx, fromY );
			if ( toY < fromY )
			{
				for (int i = fromY; i > toY; i -= step)
				{
					toaster.setLocation(posx, i);
					Thread.sleep(stepTime);
				}
			}
			else
			{
				for (int i = fromY; i < toY; i += step)
				{
					toaster.setLocation(posx, i);
					Thread.sleep(stepTime);
				}
			}
			toaster.setLocation( posx, toY );
		}
