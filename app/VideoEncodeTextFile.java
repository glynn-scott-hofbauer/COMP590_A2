package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class VideoEncodeTextFile {

	public static void main(String[] args) throws IOException {
		String input_file_name = "video-data/out.dat";
		String output_file_name = "video-data/static-compressed.dat";
		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		
		
		//creates array of symbols of -255 to 255 representing the possible difference values 
		int num_symbols = (int) new File(input_file_name).length();
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
		
		
		FileInputStream fis = new FileInputStream(input_file_name);
		
		//adds the difference counts to counts array
		int first_byte = fis.read();
		int keep_byte = first_byte;
		while(fis.available() > 0){
			int next_byte = fis.read();
			int difference = difference(first_byte, next_byte);
			int placement = 255 + difference;
			counts[placement]++;
			first_byte = next_byte;
		}
		fis.close();
				
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, counts);
		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);
//
		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);
		
		
//		for(int i =  0; i<counts.length; i++){
//			System.out.println(counts[i]);
//		}
		print(symbols, counts);
		
		
//		bit_sink.write(keep_byte, 8);
		for (int i=0; i<512; i++) {
			bit_sink.write(counts[i], 32);
		}
		
		
		bit_sink.write(keep_byte, 8);
		bit_sink.write(num_symbols, 32);
		
//		print(symbols, counts);
		System.out.println("Now encoding into the file");
		// Now encode the input
		fis = new FileInputStream(input_file_name);
		
		
		//write's the bits to the file
	
		int first = fis.read();
		
		while(fis.available()>0){
			int next_symbol = fis.read();
			int difference = difference(first, next_symbol);
			encoder.encode(difference, model, bit_sink);
			first = next_symbol;
	
		}
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		int length = (int) new File(output_file_name).length();
		System.out.println("Original Uncompressed File Size: "+ num_symbols);
		System.out.println("Compressed File Size: " + length);
		System.out.println("Compression of " + (num_symbols - length)  + " bytes" );
		
		
		System.out.println("Done");
	}
	
	
	public static int difference(int x, int y){
		if(x>y){
			return -(x-y);
		}else if(y>x){
			return y-x;
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
