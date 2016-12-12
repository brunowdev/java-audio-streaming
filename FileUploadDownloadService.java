package com.edu.abhi.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author Bruno Bitencourt Luiz
 *
 */
@Path("/arquivos")
public class FileUploadDownloadService {

	private static String FILE_PATH = "";

	private static final int TAM_1MB = 1024 * 1024;

	@GET
	@Path("/play/{fileName}")
	@Produces("audio/mp3")
	public Response streamAudio(@HeaderParam("Range") String range, @PathParam("fileName") String fileName) throws Exception {
		FILE_PATH = "C:\\temp\\" + fileName;
		final File objFile = new File(FILE_PATH);
		return buildStream(objFile, range);
	}

	int size = 0;

	private Response buildStream(final File asset, final String range) throws Exception {

		// range não foi passado -> No Firefox, Opera, IE não é passado range na
		// requisição
		if (range == null) {

			final StreamingOutput streamer = new StreamingOutput() {

				@SuppressWarnings("resource")
				@Override
				public void write(final OutputStream output) throws IOException, WebApplicationException {

					final FileChannel inputChannel = new FileInputStream(asset).getChannel();
					final WritableByteChannel outputChannel = Channels.newChannel(output);
					try {
						size = (int) inputChannel.size();
						inputChannel.transferTo(0, inputChannel.size(), outputChannel);
					} finally {
						inputChannel.close();
						outputChannel.close();
					}
				}
			};

			return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, asset.length()).header("Accept-Ranges", "bytes")
					.header("Content-Range:", "bytes " + 0 + "-" + size + "/" + size).header("Pragma", "no-cache").build();
		}

		final String[] ranges = range.split("=")[1].split("-");
		final int from = Integer.parseInt(ranges[0]);

		int to = TAM_1MB + from;
		if (to >= asset.length()) {
			to = (int) (asset.length() - 1);
		}
		if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
		final RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);

		final int len = to - from + 1;
		final MediaStreamer streamer = new MediaStreamer(len, raf);
		final Response.ResponseBuilder res = Response.status(Status.PARTIAL_CONTENT).entity(streamer).header("Accept-Ranges", "bytes").header("Content-Range", responseRange)
				.header(HttpHeaders.CONTENT_LENGTH, streamer.getTamanho()).header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));

		return res.build();
	}

}
