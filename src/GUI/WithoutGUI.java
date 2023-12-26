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
	/**Коды HTTP. глядеть сюдой: https://upload.wikimedia.org/wikipedia/commons/6/65/Http-headers-status.gif*/
	private enum HTTP_C{
		OK("OK",200),
		
		BR("Bad Request", 400),
		
		NI("Not Implemented", 501),
		;
		/**Описание ошибки*/
		public final String text;
		/**Код ошибки*/
		public final int code;
		private HTTP_C(String t, int c){text=t;code=c;}
	}
	/**Заголовок http запроса*/
	private class Header{
		/*Ключ*/
		public final String key;
		/**Параметры*/
		public final List<String> params = new ArrayList<>(1);
		
		public Header(String line){
			if(line.startsWith("GET")) {
				key = "method"; 
				add("GET");
			}else if(line.startsWith("POST")){
				key = "method"; 
				add("POST");
			} else if(line.indexOf(':') != -1){
				final var pair = line.split(":");
				key = pair[0]; 
				Arrays.stream(pair[1].split(";")).forEach(this::add);	
			} else {
				key = null;
			}
			if(line.startsWith("GET") || line.startsWith("POST")){
				final var end_host = line;
				if(end_host.lastIndexOf("/?") != -1){
					final var params_row = end_host.substring(end_host.lastIndexOf("/?") + 2,end_host.lastIndexOf(" "));
					Arrays.stream(params_row.split("&")).forEach(this::add);
				}
			}
		}
		private void add(String param){
			params.add(param.trim());
		}
		public String getFirst(){return params.get(0);}
		@Override public String toString(){return key + " " + Arrays.toString(params.toArray());}
	}

	/**Имя файла для сохранения*/
	private final String fileName;
	/**Флаг, показывающий что мы работаем*/
	private boolean isWork = true;
	
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
			while(isWork){
				var nut = System.currentTimeMillis() / 1000;
				if(reader.ready()){
					if(!in(reader.readLine()))
						System.out.println(Configurations.getProperty(WithoutGUI.class,"help"));
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
		System.out.println(Configurations.getProperty(WithoutGUI.class,"bay"));
	}
	/**Основной цикл сервера*/
	private void TCP_IP(int port){
		try (final var serverSocket = new java.net.ServerSocket(port)) {
            System.out.println(Configurations.getProperty(WithoutGUI.class,"port.start",port));
            while (isWork) {
				final var socket = serverSocket.accept(); //Ждём клиента
				socket.setSoTimeout(1000);
				
				try ( final var input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));) {
					//Заголовки
					final var headers = new HashMap<String, Header>();
					//Данные
					final var datas = new StringBuffer();
					try{
						//Считываем весь запрос
						String line;
						while (!(line = input.readLine()).isBlank()) {
							final var h = new Header(line);
							headers.put(h.key, h);
						}
						if(headers.containsKey("method") && headers.get("method").getFirst().equals("POST")){
							//Считываем тело сообщения
							if(headers.containsKey("Content-Length")){
								final var l = Integer.parseInt(headers.get("Content-Length").getFirst());
								final var s = new char[l];
								input.read(s, 0, l);
								datas.append(s);
							} else {
								final var part = 77;
								final var s = new char[part];
								int l;
								while((l = input.read(s, 0, s.length)) != -1){
									datas.append(s, 0, l);
								}
							}
							
						}
					} catch(java.net.SocketTimeoutException e){}
					final var isJSON_CT = headers.containsKey("Content-Type") && headers.get("Content-Type").getFirst().equals("application/json");
					if(headers.containsKey("method") && headers.get("method").getFirst().equals("POST") && isJSON_CT){
						//Пришёл POST запрос с JSON в центре. Можем и обработать
						try{
							final var json = new JSON(datas.toString());
							if(in(json)) sendResponse(socket,HTTP_C.OK);
							else sendResponse(socket,HTTP_C.NI);
						}catch(JSON.ParseException | IllegalArgumentException | ClassCastException e){
							sendResponse(socket,HTTP_C.BR);
						}
					} else if(headers.containsKey("method") && headers.get("method").getFirst().equals("GET")){
						final var params = headers.get("method").params;
						final var cmd = params.stream().filter(p->p.startsWith("cmd=")).findFirst().orElse(null);
						if(cmd == null || !in(cmd.substring(4))){
							sendResponse(socket,Configurations.getHProperty(WithoutGUI.class,"help"));
						} else {
							sendResponse(socket,getTitle());
						}
					} else {
						sendResponse(socket,HTTP_C.BR);
					}
				}
                socket.close();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println(Configurations.getProperty(WithoutGUI.class,"port.end"));
	}
	/** обработка команд консоли
	 * @param console пришедший запрос из консоли
	 * @return false, если пришла неизвестная команда
	 */
	private boolean in(String console){
		boolean isOk = true;
		switch (console) {
			case "p" -> {
				if(Configurations.world.isActiv()) {
					Configurations.world.awaitStop();
					printTitle();
				} else {
					Configurations.world.start();
				}
			}
			case "q" -> shutdown();
			default -> isOk = false;
		}
		return isOk;
	}
	/** Обработка команд HTTP
	 * @param http запрос
	 * @return false, если пришла неизвестная команда
	 */
	private boolean in(JSON http){
		boolean isOk = true;
		if(http.containsKey("WORLD")){
			final var doing = http.get(String.class,"WORLD");
			switch (doing) {
				case "start" -> Configurations.world.start();
				case "stop" -> Configurations.world.stop();
				case "step" -> Configurations.world.step();
				case "shutdown" -> shutdown();
				default -> isOk = false;
			}
		} else {
			isOk = false;
		}
		return isOk;
	}
	/**Отправляет клиенту сообщение
	 * @param text какой текст ему отправить
	 * @throws IOException 
	 */
	 private void sendResponse(java.net.Socket client, String text) throws IOException {
		 final var page = """
						  <!DOCTYPE html>
						  <html>
						      <head>
						          <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
						          <title> 
									"""+getTitle()+"""
								   </title>
						      </head>
						      <body>
						          """+text+"""
						      </body>
						  </html>
                    """;
		 sendResponse(client, HTTP_C.OK.code + " " + HTTP_C.OK.text, "text/html; charset=utf-8", page.getBytes());
	 }
	/**Отправляет клиенту сообщение об ошибке
	 * @param client кому
	 * @param error что за ошибка
	 * @throws IOException 
	 */
	 private static void sendResponse(java.net.Socket client, HTTP_C error) throws IOException {
		 sendResponse(client, error.code + " " + error.text, "text/html; charset=utf-8", ("<h1>" + error.text + "</h1>").getBytes());
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
		System.out.println(getTitle());
        System.out.println("-------------------\n");
	}
	private String getTitle(){
		return MessageFormat.format(Configurations.getProperty(WithoutGUI.class,"title"), world.step,
				world.pps.FPS(), world.getCount(CellObject.LV_STATUS.LV_ALIVE), world.getCount(CellObject.LV_STATUS.LV_ORGANIC),
				world.getCount(CellObject.LV_STATUS.LV_POISON), world.getCount(CellObject.LV_STATUS.LV_WALL), world.isActiv() ? ">" : "||");
	}
	/**Функция будет вызвана, когда приложению следует завершиться в режиме без GUI*/
	private void shutdown(){
		isWork = false;
		Configurations.world.awaitStop();
		try {
			Configurations.save(fileName);
		} catch (IOException ex) {
			Logger.getLogger(WithoutGUI.class.getName()).log(Level.SEVERE, Configurations.getProperty(WithoutGUI.class,"shutdown.error"), ex);
		}
	}
}
