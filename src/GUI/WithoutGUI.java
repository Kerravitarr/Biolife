/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import static Calculations.Configurations.world;
import MapObjects.CellObject;
import Utils.JSON;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Это класс, который обрабатывает мир в режиме без графического интерфейса
 * @author Kerravitarr
 */
public class WithoutGUI {
	private enum TCP_E{
		BR("Bad Request", 400),
		
		NI("Not Implemented", 501),
		;
		/**Описание ошибки*/
		public final String text;
		/**Код ошибки*/
		public final int code;
		private TCP_E(String t, int c){text=t;code=c;}
	}
	/**Заголовок http запроса*/
	private class Header{
		/*Ключ*/
		public final String key;
		/**Параметры*/
		public final List<String> params = new ArrayList<>(1);
		
		public Header(String line){
			if(line.startsWith("GET")) {
				key = "method"; params.add("GET");
			}else if(line.startsWith("POST")){
				key = "method"; params.add("POST");
			} else if(line.indexOf(':') != -1){
				final var pair = line.split(":");
				key = pair[0]; params.addAll(Arrays.asList(pair[1].split(";")));
			} else {
				key = null;
			}
		}
	}
	
	/**Имя файла для сохранения*/
	private final String fileName;
	
	public static void start(String fn, int port){
		new Thread(()->new WithoutGUI(fn, port)).start();
	}
	
	private WithoutGUI(String fn, int port) {
		//Контролируем ctrl+c
		Runtime.getRuntime().addShutdownHook(new Thread() {@Override public void run() { shutdown();}});
		fileName = fn;
		//Запускаем сервер, через который будет происходить общение
		new Thread(()->TCP_IP(port)).start();
		
		//Ну помчали!
		loop();
	}
	/**Основной цикл математики*/
	private void loop(){
		printTitle();
		System.out.println(Configurations.getProperty(WithoutGUI.class,"help"));
		
		try(final var reader = new BufferedReader(new InputStreamReader(System.in));){
			var lut = System.currentTimeMillis() / 1000;
			while(true){
				var nut = System.currentTimeMillis() / 1000;
				if(reader.ready()){
					final var ch = reader.readLine();
					switch (ch) {
						case "p" -> {
							if(Configurations.world.isActiv()) {
								Configurations.world.awaitStop();
								printTitle();
							} else {
								Configurations.world.start();
							}
						}
						case "q" -> {
							shutdown();
							return;
						}
						default -> {
							System.out.println(Configurations.getProperty(WithoutGUI.class,"help"));
						}
					}
				}
				if(nut - lut > 10){ //Каждые 60 с
					if(Configurations.world.isActiv()){
						printTitle();
						//Автосохранение
						if(Configurations.world.getCount(CellObject.LV_STATUS.LV_ALIVE) > 0 && Math.abs(world.step - Configurations.confoguration.lastSaveCount) > Configurations.confoguration.SAVE_PERIOD){
							var list = new File[Configurations.confoguration.COUNT_SAVE];
							for(var i = 0 ; i < Configurations.confoguration.COUNT_SAVE ; i++){
								list[i] = new File("autosave" + (i+1) + ".zbmap");
							}
							var save = list[0];
							for(var i = 1 ; i < Configurations.confoguration.COUNT_SAVE && save.exists(); i++){
								if(!list[i].exists() || save.lastModified() > list[i].lastModified())
									save = list[i];
							}
							Configurations.save(save.getName());
						}
					}
					lut = nut;
				}
				Utils.Utils.pause(1);
			}
		} catch(IOException ex){
			shutdown();
			System.err.println(ex);
			System.err.println("Закончили работу...");
		}
	}
	/**Основной цикл сервера*/
	private void TCP_IP(int port){
		try (final var serverSocket = new java.net.ServerSocket(port)) {
            System.out.println(Configurations.getProperty(WithoutGUI.class,"port.start",port));
 
            while (true) {
                final var socket = serverSocket.accept(); //Ждём клиента
				
				try ( final var input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));) {
					
					//Заголовки
					final var headers = new HashMap<String, Header>();
					
					//Считываем весь запрос
					String line;
					while (!(line = input.readLine()).isBlank()) {
						final var h = new Header(line);
						headers.put(h.key, h);
					}
					if(!headers.containsKey("ContentType") || !headers.get("ContentType").params.get(0).equals("application/json")){
						sendResponse(socket,TCP_E.BR);
					} else {
						sendResponse(socket,TCP_E.NI);
					}
				}
                socket.close();
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
	}
	/**Отправляет клиенту сообщение об ошибке
	 * @param client кому
	 * @param error что за ошибка
	 * @throws IOException 
	 */
	 private static void sendResponse(java.net.Socket client, TCP_E error) throws IOException {
		 sendResponse(client, error.code + " " + error.text, "text/html", ("<h1>" + error.text + "</h1>").getBytes());
	 }
	 /** Отправляет сообщение клиенту
	  * @param client кому
	  * @param status какой статус
	  * @param contentType какой тип информации
	  * @param content что за данные
	  * @throws IOException 
	  */
	 private static void sendResponse(java.net.Socket client, String status, String contentType, byte[] content) throws IOException {
		try (final var clientOutput = client.getOutputStream()) {
			clientOutput.write(("HTTP/1.1 " + status).getBytes());
			clientOutput.write(("\r\nContentType: " + contentType + "\r\n").getBytes());
			clientOutput.write("\r\n".getBytes());
			clientOutput.write(content);
			clientOutput.write("\r\n\r\n".getBytes());
			clientOutput.flush();
		}
    }
	
	/**Печатает заголовок в режиме без GUI*/
	private void printTitle(){
		System.out.println("\n-------------------");
		String title = MessageFormat.format(Configurations.getProperty(WithoutGUI.class,"title"), world.step,
				world.pps.FPS(), world.getCount(CellObject.LV_STATUS.LV_ALIVE), world.getCount(CellObject.LV_STATUS.LV_ORGANIC),
				world.getCount(CellObject.LV_STATUS.LV_POISON), world.getCount(CellObject.LV_STATUS.LV_WALL), world.isActiv() ? ">" : "||");
		System.out.println(title);
        System.out.println("-------------------\n");
	}
	/**Функция будет вызвана, когда приложению следует завершиться в режиме без GUI*/
	private void shutdown(){
		Configurations.world.awaitStop();
		try {
			Configurations.save(fileName);
		} catch (IOException ex) {
			Logger.getLogger(WithoutGUI.class.getName()).log(Level.SEVERE, Configurations.getProperty(WithoutGUI.class,"shutdown.error"), ex);
		}
	}
}
