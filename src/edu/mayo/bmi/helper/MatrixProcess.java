package edu.mayo.bmi.helper;

import Jama.Matrix;

public class MatrixProcess {
	/**
	 * Initialize a M x N matrix with zeros.
	 * Requires .
	 * @param 
	 */
	public static double[][] initZeros(int m, int n){
		double[][] mat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				mat[i][j] = 0;
			}
		}
		return mat;
	}
	
	/**
	 * Initialize a M x N matrix with ones.
	 * Requires .
	 * @param 
	 */
	public static double[][] initOnes(int m, int n){
		double[][] mat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				mat[i][j] = 1;
			}
		}
		return mat;
	}
	/**
	 * Build a M x M diagonal matrix using M x 1 vector.
	 * Requires .
	 * @param 
	 */
	public static Matrix vec2Diagonal(Matrix vec){
		double[][] diag = new double[vec.getRowDimension()][vec.getRowDimension()];
		for(int i=0;i<vec.getRowDimension();i++){
			for(int j=0;j<vec.getRowDimension();j++){
				if(i == j)
					diag[i][j] = vec.get(i, 0);
				else
					diag[i][j] = 0;
			}
		}
		Matrix diagMatrix = new Matrix(diag);
		return diagMatrix;
	}
}
