import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.math.BigDecimal;

/*
 * Disclaimer:
1. I have not implemented the Backspace key or button, which is reserved by the "NA" button right now.
2. Subsequent decimal points following the first decimal point are ignored.
3. I have not implemented showing the formula before "=" is clicked, as does Microsoft calculator. I mimic the Iphone 
calculator or simple hand-held calculator that it only shows the current operand and when an operator is pressed, 
it displays the interim result.
4. Using the "=" key combined with Shift to indicates a "+" has some quarkiness, sometimes it does not show
current result as it normally would. I believe this is because the timing of setting the textfield text and keyPress events 
internal handling. I will look into that when I have time.

 */
public class ViewerMK2 
{
	//JFrame Stuff
	public static JFrame frame = new JFrame();
	public static JPanel mainPanel = new JPanel();
	public static JPanel buttonPanel = new JPanel();

	//All Buttons and labels corresponding to each button
	public static JButton[] buttons = new JButton[20];
	public static String[] labels = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "+",  "-",  "*",  "/", "=", "CE", "C", "+/-", "NA"};
								   // 0	   1    2    3    4    5    6    7    8    9   10    11    12    13    14   15   16    17   18     19
	
	//JTextField
	public static JTextField textField = new JTextField();
	
	//arrays of numbers and operators
	public static ArrayList<Double> numbers = new ArrayList<Double>();
	public static ArrayList<String> operators = new ArrayList<String>();
	
	//variables we need in action listener and key listener
	public static double currentNumber = 0.0; //temporary number that user is creating at the moment
	public static double result = 0.0; //the end result after equation-ing
	public static int numberOfDecimalPoints = 1;
	public static boolean isNegative = false;
	
	/*
	 * states: 
	 * 1 - only accepts numbers and decimal point
	 * 2 - accepts both numbers, operators, and equal sign
	 * 3 - after "=" is pressed
	 * 4 - after "." is pressed
	 */
	public static int state = 1; 
	
	public static void main(String[] args)
	{
		//setting up frame
		frame.setSize(600, 700);
		frame.setTitle("Calculator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//setting up buttons
		for(int i = 0; i < buttons.length; i++)
		{
			buttons[i] = new JButton( labels[i] );
			buttons[i].setFont(new Font("Arial", Font.PLAIN, 20));
		}
		
		textField.setEditable(false);
		textField.setFont(new Font("Arial", Font.PLAIN, 50));
		
		//Action Listener - Button Listener for JButton
		class ButtonListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e) 
			{
				int buttonNumber = 0; //number corresponding to pressed button
				
				//find number corresponding to pressed button
				for(int i = 0; i < buttons.length; i++)
				{
					if( e.getSource().equals(buttons[i]) )
					{
						buttonNumber = i;
					}
				}
				
				//what happens in state 1; only numbers are accepted
				if( state == 1 )
				{
					textField.setText("");
					//if number is clicked
					if( buttonNumber <= 9 )
					{
						currentNumber = Double.parseDouble( labels[buttonNumber] );
						state = 2;
						
						//print number to textField
						textField.setText( textField.getText() + Integer.toString(buttonNumber) );
					}
					//if "." pressed
					else if ( buttonNumber == 10 )
					{
						textField.setText(".");
						state = 4;
					}
					//if "+/-" pressed
					else if ( buttonNumber == 18 )
					{
						//switches + to -  or  - to +
						isNegative = !isNegative;
						
						if(isNegative)
						{
							textField.setText( "-" + textField.getText() );
						}
						else
						{
							textField.setText( textField.getText().substring(1) );
						}
					}
					//if "C" pressed
					else if ( buttonNumber == 17 )
					{
						clearEverything();
					}
				}
				//what happens in state 2
				else if( state == 2 )
				{
					//if user clicks number
					if( buttonNumber <= 9 )
					{
						currentNumber = ( currentNumber * 10 ) + Double.parseDouble( labels[buttonNumber] );
						
						//print number to textField
						textField.setText( textField.getText() + Integer.toString(buttonNumber) );
						
					}
					//if "+/-" pressed
					else if ( buttonNumber == 18 )
					{
						//switches + to -  or  - to +
						isNegative = !isNegative;
						
						if(isNegative)
						{
							textField.setText( "-" + textField.getText() );
						}
						else
						{
							textField.setText( textField.getText().substring(1) );
						}
					}
					//if user clicks operator
					else if( ( buttonNumber <= 14 ) && ( buttonNumber >= 11 ) )
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						//add last typed number to array of numbers
						saveNumberAndReset();
						
						//add pressed operator to operators array
						operators.add( labels[buttonNumber] );
						
						//show current result
						getResult( numbers, operators );
						
						//state = 1 because cannot end equation on operator
						state = 1;
					}
					//if "CE" Pressed
					else if ( buttonNumber == 16 )
					{
						clearEntry();
					}
					//if "C" pressed
					else if ( buttonNumber == 17 )
					{
						clearEverything();
					}
					//if "." pressed
					else if ( buttonNumber == 10 )
					{
						textField.setText( textField.getText() + "." );
						
						state = 4;
					}
					//if user clicks "="
					else if ( buttonNumber == 15 )
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						//add last typed number to array of numbers
						saveNumberAndReset();
						
						//solve equation
						getResult( numbers, operators );
												
						//reset numbers array, operators array, and cache
						resetArrays();

						
						//move to state 3
						state = 3;
					}
				}
				//what happens in state 3, after "=" pressed
				else if( state == 3 )
				{
					//if a number is pressed
					if( buttonNumber <= 9 )
					{
						//reset result
						result = 0.0;
						
						//print pressed number to textField
						textField.setText( Integer.toString( buttonNumber ) );
						
						//make currentNumber pressedNumber
						currentNumber = Double.parseDouble( labels[buttonNumber] );
						
						//change state to 2 so next input can be anything
						state = 2;
					}
					//"." is clicked
					if( buttonNumber == 10 )
					{
						//reset result
						result = 0.0;
						
						textField.setText(".");
						
						state = 4;
					}
					//if "+/-" pressed
					else if ( buttonNumber == 18 )
					{	
						if( result < 0 )
						{
							isNegative = true;
						}
						else
						{
							isNegative = false;
						}
						
						//switches + to -  or  - to +
						isNegative = !isNegative;
						
						if(isNegative)
						{
							textField.setText( "-" + textField.getText() );
						}
						else
						{
							textField.setText( textField.getText().substring(1) );
						}
					}
					//if an operator is pressed
					else if ( ( buttonNumber <= 14 ) && ( buttonNumber >= 11 ) )
					{
						
						if( isNegative )
						{
							result = result * -1;
							isNegative = false;
						}
						//put result in first element of numbers
						numbers.add(result);
						currentNumber = 0;
						
						//clear textField
						//textField.setText("");
						getResult( numbers, operators );
						
						//add pressed operator to operators array
						operators.add( labels[buttonNumber] );
						
						//state = 1 because cannot end equation on operator
						state = 1;
					}
					//if "C" pressed
					else if ( buttonNumber == 17 )
					{
						clearEverything();
					}
				}
				//what happens in state 4
				else if( state == 4 )
				{
					//if number pressed
					if( buttonNumber <= 9 )
					{
						currentNumber = currentNumber + buttonNumber/Math.pow(10, numberOfDecimalPoints);
						
						textField.setText( textField.getText() + buttonNumber );
						
						numberOfDecimalPoints++;
					}
					//if "+/-" pressed
					else if ( buttonNumber == 18 )
					{
						//switches + to -  or  - to +
						isNegative = !isNegative;
						
						if(isNegative)
						{
							textField.setText( "-" + textField.getText() );
						}
						else
						{
							textField.setText( textField.getText().substring(1) );
						}
					}
					//if operator is pressed
					else if ( ( buttonNumber <= 14 ) && ( buttonNumber >= 11 ) )
					{
						numberOfDecimalPoints = 1;
						
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						saveNumberAndReset();
						
						operators.add( labels[buttonNumber] );
						
						getResult( numbers, operators );
						
						state = 1;
					}
					//if "=" pressed
					else if ( buttonNumber == 15 )
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						//add last typed number to array of numbers
						saveNumberAndReset();
						
						//solve equation
						getResult( numbers, operators );
						
						//reset numbers array, operators array, and cache
						resetArrays();

						
						
						//move to state 3
						state = 3;
					}
					//if "CE" Pressed
					else if ( buttonNumber == 16 )
					{
						clearEntry();
					}
					//if "C" pressed
					else if ( buttonNumber == 17 )
					{
						clearEverything();
					}
				}
			}
		}
		
		//Action Listener - Keyboard Listener for JTextField
		class KeyboardListener implements KeyListener
		{
			public void keyReleased(KeyEvent e) 
			{
				//what happens in state 1
				if( state == 1 )
				{
					textField.setText("");
					//if user types a number
					if( ( e.getKeyChar() == '0' ) ||
						( e.getKeyChar() == '1' ) ||
						( e.getKeyChar() == '2' ) ||
						( e.getKeyChar() == '3' ) ||
						( e.getKeyChar() == '4' ) ||
						( e.getKeyChar() == '5' ) ||
						( e.getKeyChar() == '6' ) ||
						( e.getKeyChar() == '7' ) ||
						( e.getKeyChar() == '8' ) ||
						( e.getKeyChar() == '9' ) )
					{
						//makes  number pressed currentNumber
						double number = Double.parseDouble( String.valueOf( e.getKeyChar() ) );
						currentNumber = number;
						
						//change state to 2 so next input can be anything
						state = 2;
						
						textField.setText( textField.getText()+ String.valueOf( e.getKeyChar() ) );
					}
					//if user types "."
					else if ( e.getKeyChar() == '.')
					{
						state = 4;
						
						textField.setText( textField.getText()+ String.valueOf( e.getKeyChar() ) );
					}
				}
				//what happens in state 2
				else if ( state == 2 )
				{
					//if user types a number
					if( ( e.getKeyChar() == '0' ) ||
						( e.getKeyChar() == '1' ) ||
						( e.getKeyChar() == '2' ) ||
						( e.getKeyChar() == '3' ) ||
						( e.getKeyChar() == '4' ) ||
						( e.getKeyChar() == '5' ) ||
						( e.getKeyChar() == '6' ) ||
						( e.getKeyChar() == '7' ) ||
						( e.getKeyChar() == '8' ) ||
						( e.getKeyChar() == '9' ) )
					{
						//appends number pressed to currentNumber
						double number = Double.parseDouble( String.valueOf( e.getKeyChar() ) );
						currentNumber = currentNumber * 10 + number;
						
						textField.setText( textField.getText()+ String.valueOf( e.getKeyChar() ) );
					}
					//if user types an operator
					else if( ( e.getKeyChar() == '+' ) ||
							 ( e.getKeyChar() == '-' ) ||
							 ( e.getKeyChar() == '*' ) ||
							 ( e.getKeyChar() == '/' )	)
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						//adds currentNumber to numbers array
						saveNumberAndReset();
						
						//add operator to operator array
						operators.add( String.valueOf( e.getKeyChar() ) );
						
						//show current result so far
						getResult( numbers, operators );
						
						//change state to 1 so next input must be number
						state = 1;
					}
					//if user types "."
					else if ( e.getKeyChar() == '.')
					{
						state = 4;
						
						textField.setText( textField.getText()+ String.valueOf( e.getKeyChar() ) );
					}
					//if user types "="
					else if ( ( e.getKeyChar() == '=' ) || ( e.getKeyCode() == KeyEvent.VK_ENTER ) )
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						//add last typed number to array of numbers
						saveNumberAndReset();
						
						//solve equation
						getResult( numbers, operators );
												
						//reset numbers array, operators array, and cache
						resetArrays();
						
						//move to state 3
						state = 3;
					}
				}
				//what happens in state 3, after "=" is pressed
				else if ( state == 3 )
				{
					//if user types number
					if( ( e.getKeyChar() == '0' ) ||
						( e.getKeyChar() == '1' ) ||
						( e.getKeyChar() == '2' ) ||
						( e.getKeyChar() == '3' ) ||
						( e.getKeyChar() == '4' ) ||
						( e.getKeyChar() == '5' ) ||
						( e.getKeyChar() == '6' ) ||
						( e.getKeyChar() == '7' ) ||
						( e.getKeyChar() == '8' ) ||
						( e.getKeyChar() == '9' ) )
					{
						//reset result
						result = 0.0;
						
						//print number to textField
						textField.setText( String.valueOf( e.getKeyChar() ) );
						
						//make currentNumber = number typed
						double number = Double.parseDouble( String.valueOf( e.getKeyChar() ) );
						currentNumber = number;
						
						//change state to 2 so that next input can be anything
						state = 2;
					}
					//if user types "."
					else if ( e.getKeyChar() == '.' )
					{
						//reset result
						result = 0.0;
						
						textField.setText( "." );
						
						state = 4;
					}
					//if user types operator
					else if( ( e.getKeyChar() == '+' ) ||
							 ( e.getKeyChar() == '-' ) ||
							 ( e.getKeyChar() == '*' ) ||
							 ( e.getKeyChar() == '/' )	|| e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_MULTIPLY || e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_DIVIDE)
					{
						//put result in first element of numbers
						numbers.add(result);
						
						currentNumber = 0.0;
						
						getResult( numbers, operators );
						
						//add pressed operator to operators array
						operators.add( String.valueOf( e.getKeyChar() ) );
						
						//state = 1 because cannot end equation on operator
						state = 1;
						
					}
				}
				//what happens in state 4
				else if ( state == 4 )
				{
					//if user types number
					if( ( e.getKeyChar() == '0' ) ||
						( e.getKeyChar() == '1' ) ||
						( e.getKeyChar() == '2' ) ||
						( e.getKeyChar() == '3' ) ||
						( e.getKeyChar() == '4' ) ||
						( e.getKeyChar() == '5' ) ||
						( e.getKeyChar() == '6' ) ||
						( e.getKeyChar() == '7' ) ||
						( e.getKeyChar() == '8' ) ||
						( e.getKeyChar() == '9' ) )
					{
						currentNumber = currentNumber + Double.valueOf( String.valueOf( e.getKeyChar() ) )/Math.pow(10, numberOfDecimalPoints);
						
						textField.setText( textField.getText()+ String.valueOf( e.getKeyChar() ) );
						
						numberOfDecimalPoints++;
					}
					//if operator is pressed
					else if( ( e.getKeyChar() == '+' ) ||
							 ( e.getKeyChar() == '-' ) ||
							 ( e.getKeyChar() == '*' ) ||
							 ( e.getKeyChar() == '/' ))
					{
						numberOfDecimalPoints = 1;
						
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						saveNumberAndReset();
						
						operators.add( String.valueOf( e.getKeyChar() ) );
						
						getResult( numbers, operators );
						
						state = 1;
					}
					
					//if operator is pressed using shift and * +
					else if( e.getKeyCode() != KeyEvent.VK_UNDEFINED )
					{
						if(e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_MULTIPLY)
						numberOfDecimalPoints = 1;
						
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						saveNumberAndReset();
						
						operators.add( String.valueOf( e.getKeyChar() ) );
						
						getResult( numbers, operators );
						
						state = 1;
					}
					
					//if user types "="
					else if ( ( e.getKeyChar() == '=' ) || ( e.getKeyCode() == KeyEvent.VK_ENTER ) )
					{
						if( isNegative )
						{
							currentNumber *= -1;
							isNegative = false;
						}
						
						//add last typed number to array of numbers
						saveNumberAndReset();
						
						//solve equation
						getResult( numbers, operators );
												
						//reset numbers array, operators array, and cache
						resetArrays();

						
						//move to state 3
						state = 3;
					}
					//if "CE" Pressed
					/*else if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE )
					{
						clearEntry();
					}*/
				}
			}
			
			//do nothing methods
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
			
		}
		//creating Event Listeners
		ActionListener buttonListener = new ButtonListener();
		KeyListener keyboardListener = new KeyboardListener();
		
		//adding listeners to buttons
		for(JButton temp : buttons)
		{
			temp.addActionListener(buttonListener);
		}
		
		//adding keyboardListener everywhere
		textField.addKeyListener(keyboardListener);
		for(int i = 0; i< buttons.length; i++)
		{
			buttons[i].addKeyListener(keyboardListener);
		}
		
		//creating layouts for panels
		mainPanel.setLayout( new BorderLayout() );
		buttonPanel.setLayout( new GridLayout( 5,4,3,3 ) );
		
		for(int i = 0; i < buttons.length; i++)
		{
			int red = (int)(Math.random() * 255);
			int green = (int)(Math.random() * 255);
			int blue = (int)(Math.random() * 255);
			buttons[i].setBackground( new Color( red, green, blue ) );
		}
		
		//adding buttons to panel
		buttonPanel.add( buttons[16] );
		buttonPanel.add( buttons[17] );
		buttonPanel.add( buttons[19] );
		buttonPanel.add( buttons[14] );
		buttonPanel.add( buttons[7] );
		buttonPanel.add( buttons[8] );
		buttonPanel.add( buttons[9] );
		buttonPanel.add( buttons[13] );
		buttonPanel.add( buttons[4] );
		buttonPanel.add( buttons[5] );
		buttonPanel.add( buttons[6] );
		buttonPanel.add( buttons[12] );
		buttonPanel.add( buttons[1] );
		buttonPanel.add( buttons[2] );
		buttonPanel.add( buttons[3] );
		buttonPanel.add( buttons[11] );
		buttonPanel.add( buttons[18] );
		buttonPanel.add( buttons[0] );
		buttonPanel.add( buttons[10] );
		buttonPanel.add( buttons[15] );
		
		//setting up JTextField
		int red = (int)(Math.random() * 255);
		int green = (int)(Math.random() * 255);
		int blue = (int)(Math.random() * 255);
		int red2 = (int)(Math.random() * 255);
		int green2 = (int)(Math.random() * 255);
		int blue2 = (int)(Math.random() * 255);
		textField.setBorder( BorderFactory.createLineBorder( new Color( red, green, blue ) , 10) );
		
		buttonPanel.setBorder( BorderFactory.createLineBorder( new Color( red2, green2, blue2 ) , 10, true) );
		
		//adding panel and panel2 to mainPanel
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		mainPanel.add(textField, BorderLayout.NORTH);
		
		//adding things to the frame
		frame.add(mainPanel);
		
		//setting focus on textField upon startup
		frame.addWindowListener
		( 
			new WindowAdapter() 
			{
				public void windowOpened( WindowEvent e )
				{
					textField.requestFocus();
				}
			}
		);
		
		//making frame visible
		frame.setVisible(true);
	}
	
	public static void getResult( ArrayList<Double> numbers, ArrayList<String> operators )
	{
		//solve equation
		BigDecimal temp = BigDecimal.valueOf(numbers.get(0));;
		for(int i = 1; i < numbers.size(); i++)
		{
			if( operators.get( i-1 ).equals( "+" ) )
			{
				temp = temp.add(BigDecimal.valueOf(numbers.get(i)));
			}
			else if( operators.get( i-1 ).equals( "-" ) )
			{
				//temp = temp - numbers.get( i );
				temp = temp.subtract(BigDecimal.valueOf(numbers.get(i)));
			}
			else if( operators.get( i-1 ).equals( "*" ) )
			{
				//temp = temp * numbers.get( i );
				temp = temp.multiply(BigDecimal.valueOf(numbers.get(i)));
			}
			else if( operators.get( i-1 ).equals( "/" ) )
			{
				//temp = temp / numbers.get( i );
				temp = temp.divide(BigDecimal.valueOf(numbers.get(i)));
			}
		}
		
		result = temp.doubleValue();
		numberOfDecimalPoints = 1;
		//textField.setText(parse(result));
		SwingUtilities.invokeLater(new Runnable() 
	    {
	      public void run()
	      {
				textField.setText(parse(result));
	      }
	    });
	}
	
	public static void saveNumberAndReset()
	{
		numbers.add( currentNumber );
		currentNumber = 0.0;
	}
	
	public static void resetArrays()
	{
		numbers = new ArrayList<Double>();
		operators = new ArrayList<String>();
	}
	
	public static void clearEntry()
	{
		currentNumber = 0.0;
		isNegative = false;
		textField.setText("");
		state = 1;
		numberOfDecimalPoints = 1;
	}
	public static void clearEverything()
	{
		currentNumber = 0.0;
		result = 0.0;
		numberOfDecimalPoints = 1;
		isNegative = false;
		resetArrays();
		state = 1;
		textField.setText("");
	}
	
	public static String parse(double num) 
	{
	    if((int) num == num) return Integer.toString((int) num); 
	    return String.valueOf(num);
	}
}