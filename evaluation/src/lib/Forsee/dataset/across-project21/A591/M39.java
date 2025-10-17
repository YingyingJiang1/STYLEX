		private void initComponents()
		{

			setSize(toasterWidth, toasterHeight);
			message.setFont( getToasterMessageFont() );
			JPanel externalPanel = new JPanel(new BorderLayout(1, 1));
			externalPanel.setBackground( getBorderColor() );
			JPanel innerPanel = new JPanel(new BorderLayout( getMargin(), getMargin() ));
			innerPanel.setBackground( getToasterColor() );
			message.setBackground( getToasterColor() );
			message.setMargin( new Insets( 2,2,2,2 ) );
			message.setLineWrap( true );
			message.setWrapStyleWord( true );

			EtchedBorder etchedBorder = (EtchedBorder) BorderFactory
					.createEtchedBorder();
			externalPanel.setBorder(etchedBorder);

			externalPanel.add(innerPanel);
      message.setForeground( getMessageColor() );
			innerPanel.add(iconLabel, BorderLayout.WEST);
			innerPanel.add(message, BorderLayout.CENTER);
			getContentPane().add(externalPanel);
		}
