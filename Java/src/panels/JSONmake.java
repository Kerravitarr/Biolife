package panels;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONmake {
	private class JSONParametr{
		String key = null;
		String value_s = null;
		String[] value_ms = null;
		JSONmake value_j = null;
		JSONmake[] value_js = null;
		
		public JSONParametr(String key, String value) {
			this.key = key;
			this.value_s = value;
		}

		public JSONParametr(String key, String[] value) {
			this.key = key;
			this.value_ms = value;
		}

		public JSONParametr(String key, JSONmake value) {
			this.key = key;
			this.value_j = value;
		}
		
		public JSONParametr(String key, JSONmake[] value) {
			this.key = key;
			this.value_js = value;
		}
		
		public String toString() {
			if(value_s != null) {
				return "\"" + key + "\":" + value_s;
			}else if(value_ms != null) {
				String vals = "";
				for(String i : value_ms) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i;
				}
				return "\"" + key + "\":[" + vals + "]";
			}else if(value_j != null) {
				return "\"" + key + "\":" + value_j.toJSONString() + "";
			}else if(value_js != null) {
				String vals = "";
				for(JSONmake i : value_js) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i.toJSONString();
				}
				return "\"" + key + "\":[" + vals + "]";
			} else {
				throw new RuntimeException("Не верный параметр");
			}
		}
		public void write(FileWriter writer,String tabs) throws IOException {
			if(value_s != null) {
				writer.write(tabs + "\"" + key + "\":" + value_s);
			}else if(value_ms != null) {
				String vals = "";
				for(String i : value_ms) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i;
				}
				writer.write(tabs + "\"" + key + "\":[" + vals + "]");
			}else if(value_j != null) {
				writer.write(tabs + "\"" + key + "\":{\n");
				value_j.writeToFormatJSONString(writer, tabs + '\t');
				writer.write("\n" + tabs + "}");
			}else if(value_js != null) {
				boolean isFirst = true;
				writer.write(tabs + "\"" + key + "\":[\n");
				if(value_js.length == 0) {
					writer.write(tabs + "\t{}\n" + tabs + "]");
				} else {
					for(JSONmake i : value_js) {
						if(isFirst)
							isFirst = false;
						else
							writer.write("\n" + tabs + "\t},\n");
						writer.write(tabs + "\t{\n");
						i.writeToFormatJSONString(writer, tabs + "\t\t");
					}
					writer.write("\n" + tabs + "\t}\n" + tabs + "]");
				}
			} else {
				throw new RuntimeException("Не верный параметр");
			}
		}
	}

	
	
	ArrayList<JSONParametr> parametrs;
	/**Обратное чтение JSONа*/
	JSONObject parseObj = null;
	
	public JSONmake(){
		parametrs = new ArrayList<>();
	}
	public JSONmake(String parseStr) {
		JSONParser parser = new JSONParser();
		try {
			parseObj = (JSONObject) parser.parse(parseStr);
		} catch (ParseException e) {
			throw new RuntimeException(
					"Произошла ошибка при парсинге файла. Строка:" + parseStr
							+ " Ошибка: " + e.toString() + "(" + parseStr.substring(e.getPosition() - 5,e.getPosition()) + "→"  + parseStr.substring(e.getPosition(),e.getPosition()+1)+ "←"+ parseStr.substring(e.getPosition()+1,e.getPosition()+5)+")");
		}
	}

	public JSONmake(Reader in) {
		JSONParser parser = new JSONParser();
		try {
			parseObj = (JSONObject) parser.parse(in);
		} catch (ParseException e) {
			throw new RuntimeException(
					"Произошла ошибка при парсинге файла.  Ошибка: " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public JSONmake(JSONObject json) {
		parseObj = json;
	}
	public void add(String key, String value) {
		parametrs.add(new JSONParametr(key,"\"" + value.replaceAll("\"", "\\\\\"") + "\""));
	}
	public void add(String key, int value) {
		add(key,(long) value);
	}
	public void add(String key, long value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	public void add(String key, double value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	public void add(String key, String[] value) {
		JSONParametr param = new JSONParametr(key,value);
		for(int i = 0 ; i < value.length ; i ++)
			param.value_ms[i] = "\"" + value[i].replaceAll("\"", "\\\\\"") + "\"";
		parametrs.add(param);
	}
	public void add(String key, JSONmake value) {
		parametrs.add(new JSONParametr(key,value));
	}
	public void add(String key, boolean value) {
		parametrs.add(new JSONParametr(key,value ? "true" : "false"));
	}
	public void add(String key, int[] value) {
		String[] str = new String[value.length];
		for(int i = 0 ; i < value.length ; i ++)
			str[i] = value[i]+"";
		parametrs.add(new JSONParametr(key,str));
	}
	public void add(String key, JSONmake[] value) {
		parametrs.add(new JSONParametr(key,value));
	}


	public void replace(String key, String value) {
		for(JSONParametr i : parametrs) {
			if(i.key.equals(key)) {
				i.value_s = "\"" + value.replaceAll("\"", "\\\\\"") + "\"";
				break;
			}
		}
	}
	
	public String getS(String key) {
		return getT(key);
	}
	public int getI(String key) {
		return (int) getL(key);
	}
	public long getL(String key) {
		return getT(key);
	}
	public double getD(String key) {
		return getT(key);
	}
	public JSONmake getJ(String key) {
		if(parseObj == null)
			return null;
		else
			return new JSONmake((JSONObject) parseObj.get(key));
	}

	public List<Long> getAL(String key) {
		return getAT(key);
	}
	
	public List<JSONmake> getAJ(String key) {
		if (parseObj == null)
			return null;
		else {
			List<JSONmake> ret = new ArrayList<>();
			for (Object i : (JSONArray) parseObj.get(key))
				ret.add(new JSONmake((JSONObject)i));
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getAT(String key) {
		if (parseObj == null)
			return null;
		else {
			List<T> ret = new ArrayList<>();
			for (Object i : (JSONArray) parseObj.get(key))
				ret.add((T) i);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getT(String key) {
		if(parseObj == null)
			return null;
		else
			return ((T) parseObj.get(key));
	}

	public String toJSONString() {
		String jsonStr = "{";
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) jsonStr +=",";
			jsonStr += parametrs.get(i).toString();
		}
		jsonStr += "}";
		return jsonStr;
	}
	
	public void writeToFormatJSONString(FileWriter writer) throws IOException {
		writer.write("{\n");
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) writer.write(",\n");
			parametrs.get(i).write(writer,"\t");
		}
		writer.write("\n}");
	}
	public void writeToFormatJSONString(FileWriter writer,String tabs) throws IOException {
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) writer.write(",\n");
			parametrs.get(i).write(writer,tabs);
		}
	}
}
