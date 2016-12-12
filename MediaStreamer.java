package com.edu.abhi.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class MediaStreamer implements StreamingOutput {

	private int tamanho;
	private final RandomAccessFile acessoArquivo;
	final byte[] buffer = new byte[4096];

	public MediaStreamer(int tamanho, RandomAccessFile acessoArquivo) {
		this.tamanho = tamanho;
		this.acessoArquivo = acessoArquivo;
	}

	@Override
	public void write(OutputStream outputStream) throws IOException, WebApplicationException {
		try {
			while (tamanho != 0) {
				final int lido = acessoArquivo.read(buffer, 0, buffer.length > tamanho ? tamanho : buffer.length);
				outputStream.write(buffer, 0, lido);
				tamanho -= lido;
			}
		} finally {
			acessoArquivo.close();
		}
	}

	public int getTamanho() {
		return tamanho;
	}
}
