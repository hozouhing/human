package com.jeff.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class BaseDaoJDBCImpl<T> implements BaseDao<T> {

	public static final Logger logger = Logger.getLogger(BaseDaoJDBCImpl.class);

	public Class<T> clz;

	public BaseDaoJDBCImpl() {
		this.getEntityClass();
	}

	// 通过反射获取泛型的实际类型,保存到clz域
	@SuppressWarnings("unchecked")
	protected void getEntityClass() {
		Type type = getClass().getGenericSuperclass();
		if (!(type instanceof ParameterizedType)) {
			type = getClass().getSuperclass().getGenericSuperclass();
		}
		this.clz = (Class<T>) ((ParameterizedType) type)
				.getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public T getObject(String sql) {
		try {
			if (rsConverter(sql, clz).size() != 0) {
				return ((List<T>) rsConverter(sql, clz)).get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<T> getList(String sql) {
		try {
			List<T> list = (List<T>) rsConverter(sql, clz);
			if (list.size() != 0)
				return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean update(T t) {
		Statement statement = null;
		try {
			statement = OrmContext.getCurrentConnection().createStatement();
			String sql = "update '" + clz.getName() + "' set ";
			Field[] fields = clz.getDeclaredFields();
			int i = 0;
			for (Field field : fields) {
				if (i != 0)
					sql += ",";
				if (field.getType() == String.class)
					sql += field.getName() + "='" + field.get(t) + "'";
				else if (field.getType() == Integer.class)
					sql += field.getName() + "='" + field.get(t) + "'";
				i++;
			}
			statement.executeUpdate(sql);
			statement.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return false;
	}

	public boolean add(T t) {
		Statement statement = null;
		try {
			String fSql = "(";
			String vSql = "(";
			Object value;
			Class<?> type;
			Field[] fields = clz.getDeclaredFields();
			Field field;
			for (int i = 0; i < fields.length; i++) {
				field = fields[i];
				fSql += field.getName();
				type = field.getType();
				if (type == int.class) {
					vSql += String.valueOf(field.getInt(t));
				} else if (type == String.class) {
					vSql += "'" + field.get(t) + "'";
				} else if (type == float.class) {
					vSql += String.valueOf(field.getFloat(t));
				} else if (type == long.class) {
					vSql += String.valueOf(field.getFloat(t));
				} else if (type == double.class) {
					vSql += String.valueOf(field.getDouble(t));
				} else if (type == java.util.Date.class
						|| type == Integer.class || type == Double.class
						|| type == Long.class) {
					value = field.get(t);
					if (value == null)
						vSql += "null";
					else
						vSql += String.valueOf(value);
				}
				if (i != fields.length - 1) {
					vSql += ",";
					fSql += ",";
				}
			}
			vSql += ")";
			fSql += ")";
			String sql = "insert into `" + clz.getName() + "` " + fSql
					+ " values " + vSql;
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.execute(sql);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public T getById(Object id) {
		try {
			String sql = this.getSql("getById");
			Class<?> clz = id.getClass();
			if (clz == int.class || clz == Integer.class)
				sql = sql.replaceFirst("{id}", String.valueOf(id));
			else
				sql = sql.replaceFirst("{id}", "'" + id + "'");
			if (rsConverter(sql, clz).size() != 0) {
				return ((List<T>) rsConverter(sql, clz)).get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean deleteById(Object id) {
		Statement statement = null;
		try {
			String sql = this.getSql("deleteById");
			Class<?> clz = id.getClass();
			if (clz == int.class || clz == Integer.class)
				sql = sql.replaceFirst("{id}", String.valueOf(id));
			else
				sql = sql.replaceFirst("{id}", "'" + id + "'");
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.executeQuery(sql);
			statement.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}

	public Object exeSql4obj(String sqlName, Map<String, Object> params,
			Class<?> tClz) {
		try {
			String sql = this.setParams(sqlName, params);
			Object obj = null;
			if (rsConverter(sql, tClz).size() != 0)
				obj = rsConverter(sql, tClz).get(0);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * List<?> 是所有泛型的父类，可以将任何List<List<Integer>>,List<Object>,List<Map<String
	 * ,Object>>传给List<?> List<?>的地位相当于普通类型的Object的地位
	 */
	@SuppressWarnings("unchecked")
	public List<?> exeSql4list(String sqlName, Map<String, Object> params,
			Class<?> tClz) {
		try {
			String sql = this.setParams(sqlName, params);
			List<Object> list = (List<Object>) rsConverter(sql, tClz);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean exeSql4bool(String sqlName, Map<String, Object> params) {
		Statement statement = null;
		try {
			String sql = setParams(sqlName, params);
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.executeQuery(sql);
			return true;
		} catch (Exception e) {
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}

	public Object exeSql4obj(String sqlName, Object param, Class<?> tClz) {
		return this.exeSql4obj(sqlName, mapConverter(param), tClz);
	}

	public List<?> exeSql4list(String sqlName, Object param, Class<?> tClz) {
		return this.exeSql4list(sqlName, mapConverter(param), tClz);
	}

	@SuppressWarnings("unchecked")
	public T exeSql4obj(String sqlName, Object param) {
		return (T) this.exeSql4obj(sqlName, mapConverter(param), this.clz);
	}

	@SuppressWarnings("unchecked")
	public List<T> exeSql4list(String sqlName, Object param) {
		return (List<T>) exeSql4list(sqlName, mapConverter(param), this.clz);
	}

	public boolean exeSql4bool(String sqlName, Object param) {
		return this.exeSql4bool(sqlName, mapConverter(param));
	}

	/**
	 * 将rs中键值对转为了object中的对应的属性的值
	 * 
	 * @param rs
	 * @param clz
	 * @return
	 */
	protected List<?> rsConverter(String sql, Class<?> clz) {
		Statement statement = null;
		ResultSet rs = null;
		List<Object> list = new ArrayList<>();
		try {
			statement = OrmContext.getCurrentConnection().createStatement();
			rs = statement.executeQuery(sql);
			Object obj;
			Field[] fields = clz.getDeclaredFields();
			while (rs.next()) {
				obj = clz.newInstance();
				for (Field field : fields) {
					field.setAccessible(true);
					Class<?> type = field.getType();
					int i;
					try {
						i = rs.findColumn(field.getName());
					} catch (SQLException e) {
						continue;
					}
					if (type == String.class) {
						String str = rs.getString(i);
						field.set(obj, str);
					} else if (type == int.class || type == Integer.class) {
						Integer integer = rs.getInt(i);
						field.set(obj, integer);
					} else if (type == float.class || type == Float.class) {
						float f = rs.getFloat(i);
						field.set(obj, f);
					} else if (type == double.class || type == Double.class) {
						double d = rs.getDouble(i);
						field.set(obj, d);
					} else if (type == long.class || type == Long.class) {
						long l = rs.getLong(i);
						field.set(obj, l);
					} else if (type == java.util.Date.class) {
						Date date = rs.getDate(i);
						field.set(obj, date);
					}
				}
				list.add(obj);
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (rs != null)
					rs.close();
				if (statement != null)
					statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 将object中的参数转为map键值对
	 * 
	 * @param object
	 * @return
	 */
	protected Map<String, Object> mapConverter(Object object) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Field[] fields = object.getClass().getDeclaredFields();
			Object value;
			for (Field field : fields) {
				field.setAccessible(true);
				value = field.get(object);
				map.put(field.getName(), value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 为sql嵌入参数
	 * 
	 * @param sqlName
	 * @param params
	 * @param pClz
	 * @return
	 */
	protected String setParams(String sqlName, Map<String, Object> params) {
		String sql = this.getSql(sqlName);
		try {
			int tagIndex = sql.indexOf("{", 0);
			int startIndex = 0;
			int endIndex = 0;
			String paramName;
			Object paramValue;
			Class<?> paramClass;
			while (tagIndex != -1) {
				// System.out.println("sdsds");
				startIndex = sql.indexOf("{", tagIndex) + 1;
				endIndex = sql.indexOf("}", tagIndex);
				paramName = sql.substring(startIndex, endIndex);
				if (!params.containsKey(paramName))
					throw new Exception("参数不完整，找不到名字为" + paramName + "的参数");
				paramValue = params.get(paramName);
				paramClass = paramValue.getClass();
				if (paramClass == int.class || paramClass == Integer.class
						|| paramClass == float.class
						|| paramClass == Float.class
						|| paramClass == long.class || paramClass == Long.class
						|| paramClass == double.class
						|| paramClass == Double.class)
					sql = sql.replaceFirst("\\{" + paramName + "\\}",
							String.valueOf(paramValue));
				else if (paramClass == String.class)
					sql = sql.replaceFirst("\\{" + paramName + "\\}", "'"
							+ String.valueOf(paramValue) + "'");
				tagIndex = sql.indexOf("{", tagIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info(sql);
		return sql;
	}

	protected String getSql(String name) {
		try {
			Method m = this.getClass().getMethod(
					"get" + name.substring(0, 1).toUpperCase()
							+ name.substring(1) + "_sql");
			String sql = (String) m.invoke(this, new Object[] {});
			return sql;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
