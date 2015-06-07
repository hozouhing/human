package com.jeff.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BaseDaoJDBCImpl<T> implements BaseDao<T> {

	public Statement statement;

	// 获取泛型（不能在运行时才给出泛型的实际类型）
	public Class<T> clz;

	public BaseDaoJDBCImpl() {
		this.getEntityClass();
	}

	// 反射获取泛型到clz变量中
	@SuppressWarnings("unchecked")
	protected void getEntityClass() {
		Type type = getClass().getGenericSuperclass();
		if (!(type instanceof ParameterizedType)) {
			type = getClass().getSuperclass().getGenericSuperclass();
		}
		this.clz = (Class<T>) ((ParameterizedType) type)
				.getActualTypeArguments()[0];
	}

	public T getObject(String sql) {
		try {
			Field[] fields = clz.getDeclaredFields();
			T t = (T) clz.newInstance();
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.setMaxRows(1);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next())
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
						field.set(t, str);
					} else if (type == Integer.class || type == int.class) {
						Integer integer = rs.getInt(i);
						field.set(t, integer);
					}
				}
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<T> getList(String sql) {
		try {
			List<T> list = new ArrayList<T>();
			Field[] fields = clz.getDeclaredFields();
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.setMaxRows(1);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				T t = (T) clz.newInstance();
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
						field.set(t, str);
					} else if (type == Integer.class) {
						Integer integer = rs.getInt(i);
						field.set(t, integer);
					}
				}
				list.add(t);
			}
			rs.close();
			statement.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean update(T t) {
		try {
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
			statement.executeQuery(sql);
			statement.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean add(T t) {
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
				} else if (type == Integer.class) {
					value = field.get(t);
					if (value == null)
						vSql += "null";
					else
						vSql += String.valueOf(value);
				} else if (type == String.class) {
					vSql += "'" + field.get(t) + "'";
				} else if (type == float.class) {
					vSql += String.valueOf(field.getFloat(t));
				} else if (type == Float.class) {
					value = field.get(t);
					if (value == null)
						vSql += "null";
					else
						vSql += String.valueOf(value);
				} else if (type == long.class) {
					vSql += String.valueOf(field.getFloat(t));
				} else if (type == Long.class) {
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
			return false;
		}

	}

	public T getById(Object id) {
		try {
			String sql = this.getSql("getById");
			Class<?> clz = id.getClass();
			if (clz == int.class || clz == Integer.class)
				sql = sql.replaceFirst("{id}", String.valueOf(id));
			else
				sql = sql.replaceFirst("{id}", "'" + id + "'");
			Field[] fields = clz.getDeclaredFields();
			T t = (T) this.clz.newInstance();
			statement = OrmContext.getCurrentConnection().createStatement();
			statement.setMaxRows(1);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next())
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
						field.set(t, str);
					} else if (type == Integer.class || type == int.class) {
						Integer integer = rs.getInt(i);
						field.set(t, integer);
					}
				}
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean deleteById(Object id) {
		try {
			String sql = this.getSql("deleteById");
			Class<?> clz = id.getClass();
			if (clz == int.class || clz == Integer.class)
				sql = sql.replaceFirst("{id}", String.valueOf(id));
			else
				sql = sql.replaceFirst("{id}", "'" + id + "'");
			statement.executeQuery(sql);
			statement.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected String getSql(String name) {
		try {
			Method m = clz.getMethod("get" + name.substring(0, 1).toUpperCase()
					+ name.substring(1) + "_sql");
			String sql = (String) m.invoke(this, new Object[] {});
			return sql;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object exeSql4obj(String sqlName, Object params, Class<?> clz) {
		return null;
	}

	public List<? extends Object> exeSql4list(String sqlName, Object params,
			Class<?> clz) {
		return null;
	}

	public T exeSql4obj(String sqlName, Object params) {
		return null;
	}

	public List<T> exeSql4list(String sqlName, Object params) {
		return null;
	}

	public boolean exeSql4bool(String sqlName, Object params) {
		return false;
	}

}
