package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class VideoDecodeTextFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "video-data/static-compressed.dat";
		String output_file_name = "video-data/video-reuncompressed.txt";

		FileInputStream fis = new FileInputStream(input_file_name);
		InputStreamBitSource bit_source = new InputStreamBitSource(fis);
		
		//creates array of symbols of -255 to 255 representing the possible difference values 
		
		int[] counts = new int[512];
		Integer[] symbols = new Integer[256*2];
		int q = -255;
		for (int i=0; i<256; i++) {
			symbols[i] = q;
			counts[i] = 0;
			q++;
		}
		
		int z = 0;
		for(int i = 255; i<512; i++){
			symbols[i] = z;
			counts[i] = 0;
			z++;
		}
		
		
		//get's the counts of the differences for differences -255-255
		int j = 0;
		while(j<512){
			counts[j] = bit_source.next(32);
			j++;
		}
		
//		print(symbols, counts);
		int first_symbol =  bit_source.next(8);
		int num_symbols = bit_source.next(32);
		

		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, counts);
		
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(40);
		
		
		FileOutputStream fos = new FileOutputStream(output_file_name);
		fos.write(first_symbol);
		for (int i=0; i<num_symbols; i++) {

			int sym = decoder.decode(model, bit_source);
			int symbol = first_symbol + sym;
			fos.write(symbol);
			first_symbol = symbol;
		}
		
		
		
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + 40);
		System.out.println("Number of symbols: " + num_symbols);

		System.out.println("Done.");

		fis.close();
	}
	
	
	public static int difference(int x, int y){
		if(x > y){
			return -(x - y);
		}else if(x< y){
			return y - x;
		}else{
			return 0;
		}
	}
	
	public static void print(Integer[] symbols, int[] counts){
		for(int i = 0; i<counts.length; i++){
			System.out.println(symbols[i]+ " : " +counts[i]);
		}
	}
}
