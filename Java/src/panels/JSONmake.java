package panels;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
				for(String i : value_ms) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i;
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
				for(JSONmake i : value_js) {
					if(isFirst)
						isFirst = false;
					else
						writer.write("\n" + tabs + "\t},\n");
					writer.write(tabs + "\t{\n");
					i.writeToFormatJSONString(writer, tabs + "\t\t");
				}
				writer.write("\n" + tabs + "\t}\n" + tabs + "]");
			} else {
				throw new RuntimeException("Не верный параметр");
			}
		}
	}
	
	ArrayList<JSONParametr> parametrs;
	
	public JSONmake(){
		parametrs = new ArrayList<>();
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
