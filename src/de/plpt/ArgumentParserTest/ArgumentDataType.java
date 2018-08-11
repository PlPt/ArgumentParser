package de.plpt.ArgumentParserTest;

import java.awt.Point;

public class ArgumentDataType {
	//region varDef
	int i = 0;
	Point p;
	//endregion

	/**
	 * Test constructor for argument parser type conversion input
	 * @param input
	 */
	public ArgumentDataType(String input) {
		String[] arr = input.split(",");

		i = Integer.parseInt(arr[0]);
		p = new Point(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
	}
}
