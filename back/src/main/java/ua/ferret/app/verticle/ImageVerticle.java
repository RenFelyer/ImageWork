package ua.ferret.app.verticle;

import static java.util.function.Predicate.not;
import static ua.ferret.app.AppEnvironment.IMAGES_PATH;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ua.ferret.app.zutils.ImageUtils;

@Component
public class ImageVerticle extends AbstractVerticle {

	private static Logger log = LoggerFactory.getLogger(ImageVerticle.class);

	@Override
	public void start(Promise<Void> promise) throws Exception {
		vertx.eventBus().consumer("image.info").handler(this::info);
		vertx.eventBus().<String>consumer("image.upload").handler(this::upload);
		vertx.eventBus().<String>consumer("image.delete").handler(this::delete);
		vertx.eventBus().<String>consumer("image.selected").handler(this::selected);

		vertx.fileSystem().readDir(IMAGES_PATH).map(this::pathToName).onSuccess(fileName -> {
			if (ImageUtils.hasImage(fileName))
				vertx.eventBus().publish("buffer.selected", fileName);
			log.info("{} launched with the selected file: {}", getClass().getSimpleName(), fileName);
			promise.complete();
		}).onFailure(promise::fail);
	}

	public void info(Message<Object> message) {
		var json = new JsonObject();
		json.put("filename", getFileName());
		json.put("filelist", getFileList());
		message.reply(json);
	}

	public void upload(Message<String> message) {
		var fileList = getFileList();
		var tempName = message.body();

		fileList.add(tempName);
		vertx.eventBus().publish("buffer.selected", tempName);
		vertx.eventBus().publish("image.selected", tempName);
		config().put("filename", tempName);
	}

	public void delete(Message<String> message) {
		var fileList = getFileList();
		var tempName = message.body();

		fileList.remove(tempName);
		
		vertx.eventBus().publish("image.selected", getFileName());
		vertx.fileSystem().delete(IMAGES_PATH + tempName);
	}

	public void selected(Message<String> message) {
		var fileName = getFileName();
		var fileList = getFileList();
		var tempName = message.body();

		if (fileName.equals(tempName) || !fileList.contains(tempName) || !ImageUtils.hasImage(tempName))
			return;

		vertx.eventBus().publish("buffer.selected", tempName);
		config().put("filename", tempName);
	}

	private JsonArray getFileList() {
		var fileList = config().getJsonArray("filelist");
		if (fileList == null) {
			fileList = new JsonArray();
			config().put("filelist", fileList);
		}
		return fileList;
	}

	private String getFileName() {
		var fileList = getFileList();
		var fileName = config().getString("filename");
		if ((fileName == null || !fileList.contains(fileName)) && !fileList.isEmpty()) {
			fileName = fileList.getString(0);
			config().put("filename", fileName);
		}
		return fileName;
	}

	private String pathToName(List<String> paths) {
		var fileList = getFileList();
		paths.stream().filter(ImageUtils::hasImage).map(File::new).map(File::getName).filter(not(fileList::contains))
				.forEach(fileList::add);
		return getFileName();
	}

}
