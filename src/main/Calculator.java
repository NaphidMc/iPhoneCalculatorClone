package main;

import java.math.BigDecimal;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Calculator extends Application {
	
	static final String CSS_PATH_PREFIX = "resources/";
	BigDecimal firstNum = new BigDecimal(0.0);
	boolean operationActive = false, lastButtonPressWasOperation;
	enum Operations {
		DIVISION, MULTIPLICATION, SUBTRACTION, ADDITION, NONE
	}
	Operations currentOperation = Operations.NONE;
	Operation lastOperation = null;
	
	class Operation {
		public Operations operation;
		BigDecimal num;
		public Operation(Operations op, BigDecimal secondNumber) {
			operation = op;
			num = secondNumber;
		}
	}
	
	Scene scene;
	Button[][] buttons = new Button[5][4];
	Label output = new Label("");
	String[][] buttonTexts = new String[][] {
		{"C", "±", "%", "÷"},
		{"7", "8", "9", "X"},
		{"4", "5", "6", "-"},
		{"1", "2", "3", "+"},
		{"0", "_", ".", "="},
	};
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Calculator");
		
		Pane pane = new Pane();
		scene = new Scene(pane, 525, 700);
		double buttonSize = scene.getWidth() / 4;
		double buttonOffsetY = scene.getHeight() - 5 * buttonSize * .85;
		
		for(int i = 0; i < 5; i++) {
			for(int k = 0; k < 4; k++) {
				if(i == 4 && k == 1)
					continue;
				Button b;
				buttons[i][k] = b = new Button(buttonTexts[i][k]);
				
				// Adds number event handlers
				if(k <= 2 && i > 0 && i < 4 || (i == 4 && k == 0)) {
					final int row = i, column = k;
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							if(row == 4 && column == 0)
								appendNumber("0");
							else 
								appendNumber((7 + column) - (row - 1) * 3 + "");
						}
						
					});
				}
				
				// Clear Button
				if(i == 0 && k == 0) {
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							clear();
						}
						
					});
				}
				
				// Decimal Point
				if(i == 4 && k == 2)  {
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							if(output.getText().indexOf('.') == -1 || lastButtonPressWasOperation) {
								appendNumber(".");
								lastButtonPressWasOperation = false;
							}
						}
						
					});
				}
				
				// Mult, div, add, sub: 
				if(k == 3 && i < 4) {
					final int row = i;
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							lastButtonPressWasOperation = true;
							setCurrentOperation(Operations.values()[row]);
						}
						
					});
				} else {
					lastButtonPressWasOperation = false;
				}
				
				// Equal Button
				if(k == 3 && i == 4) {
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							solve();
						}
						
					});
				}
				
				// Pos Neg Button
				if(i == 0 && k == 1) {
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							output.setText(new BigDecimal(output.getText()).negate() + "");
							fixOutputSpacing();
						}
					});
				}
				
				// Percent Button
				if(i == 0 && k == 2) {
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							output.setText((Double.parseDouble(output.getText()) / 100) + "");
							fixOutputSpacing();
						}
						
					});
				}
					
				b.setLayoutX(k * buttonSize);
				b.setLayoutY(i * buttonSize * .85 + buttonOffsetY);
				b.setPrefSize(buttonSize, buttonSize * .85);
				b.getStylesheets().clear();
				if(k == 3)
					b.getStylesheets().add(getClass().getResource(CSS_PATH_PREFIX + "goldButtons.css").toExternalForm());
				else if(i == 0)
					b.getStylesheets().add(getClass().getResource(CSS_PATH_PREFIX + "darkGreyButtons.css").toExternalForm());
				else 
					b.getStylesheets().add(getClass().getResource(CSS_PATH_PREFIX + "greyButtons.css").toExternalForm());
				
				if(i == 4 && k == 0)
					b.setPrefSize(buttonSize * 2, buttonSize * .85);
				
				pane.getChildren().add(b);
			}
		}
		
		pane.getChildren().add(output);
		output.setId("output");
		output.setLayoutY(25);
		
		pane.getStylesheets().clear();
		pane.getStylesheets().add(getClass().getResource(CSS_PATH_PREFIX + "pane.css").toExternalForm());
		
		appendNumber("0");
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public void posNegToggle() {
		output.setText(-1 * Double.parseDouble(output.getText()) + "");
	}
	
	public void solve() {
		BigDecimal newNum = new BigDecimal(0);
		if(currentOperation != Operations.NONE) {
			BigDecimal secondNumber = new BigDecimal(output.getText());
			lastOperation = new Operation(currentOperation, secondNumber);
			newNum = solve(firstNum, secondNumber, currentOperation);
			setCurrentOperation(Operations.NONE);
		} else if(lastOperation != null) {
			newNum = solve(firstNum, lastOperation.num, lastOperation.operation);
		}
		firstNum = newNum;
		setOutputText(newNum + "");
	}
	
	public BigDecimal solve(BigDecimal num1, BigDecimal num2, Operations operation) {
		if(operation == Operations.ADDITION) {
			return num1.add(num2);
		} else if(operation == Operations.SUBTRACTION) {
			return num1.subtract(num2);
		} else if(operation == Operations.MULTIPLICATION) {
			return num1.multiply(num2);
		} else if(operation == Operations.DIVISION) {
			return num1.divide(num2);
		}
		
		return BigDecimal.ZERO;
	}
	
	public void resetOperationBorderWidths() {
		buttons[0][3].setStyle("-fx-border-width: 1;");
		buttons[1][3].setStyle("-fx-border-width: 1;");
		buttons[2][3].setStyle("-fx-border-width: 1;");
		buttons[3][3].setStyle("-fx-border-width: 1;");
	}
	
	public void setCurrentOperation(Operations newOperation) {
		currentOperation = newOperation;
		
		int row = 0, col = 3;
		
		resetOperationBorderWidths();
		
		if(newOperation == Operations.DIVISION) {
			row = 0;
		} else if(newOperation == Operations.MULTIPLICATION) {
			row = 1;
		} else if(newOperation == Operations.SUBTRACTION) {
			row = 2;
		} else if(newOperation == Operations.ADDITION) {
			row = 3;
		}
		
		if(newOperation != Operations.NONE) {
			buttons[row][col].setStyle("-fx-border-width: 3;");
			operationActive = true;
		}
	}
	
	public void clear() {
		if(output.getText().equals("0")) {
			setCurrentOperation(Operations.NONE);
		} else {
			setOutputText("0");
			setCurrentOperation(currentOperation);
		}
	}
	
	public void fixOutputSpacing() {
		Text t = new Text(output.getText());
		t.setFont(new Font("Courier", 96));
		output.setLayoutX(scene.getWidth() - t.getLayoutBounds().getWidth() - 15);
	}
	
	public void setOutputText(String newText) {
		resetOperationBorderWidths();
		output.setText(newText);
		Text t = new Text(output.getText());
		t.setFont(new Font("Courier", 96));
		output.setLayoutX(scene.getWidth() - t.getLayoutBounds().getWidth() - 15);
		updateClearButtonText();
	}
	
	public void appendNumber(String text) {
		
		resetOperationBorderWidths();
		String newText = output.getText() + text;
		int len = newText.length();
		if(len > 0 && newText.charAt(0) == '0' && ((len > 1 && newText.charAt(1) != '.') && len != 1))
			newText = newText.substring(1, newText.length());
		
		if(operationActive) {
			operationActive = false;
			firstNum = new BigDecimal(output.getText());
			output.setText(text);
		} else {
			output.setText(newText);
		}
		
		fixOutputSpacing();
		updateClearButtonText();
	}
	
	public void updateClearButtonText() {
		if(output.getText().equals("0")) {
			buttons[0][0].setText("AC");
		} else {
			buttons[0][0].setText("C");
		}
	}
}
