using System;
using System.Collections.Generic;
using System.Web;
using System.Web.SessionState;
using System.Text;
using System.Data;
using System.Data.Sql;
using System.Data.SqlClient;
using System.Collections;
using System.Web.Script.Serialization;

namespace irri
{
    public class recordset
    {
        public String name;
        public ArrayList cols;
        public ArrayList rows;
    }
    /// <summary>
    /// objresponse 的摘要说明
    /// </summary>
    
    public class objresponse
    {
        public String cmdstatus;
        public recordset rd;
        public objresponse(){

        }
        public static string LOGIN_LOGINED = "-1";  // 已经登陆了，用于处理刷新页面的oninit命令。
        public static string LOGIN_FAIL = "0";
        public static string CMD_SUCESS = "1";
        public static string LOGIN_DBERROR = "2";
        public static string LOGIN_UNPWDERROR = "3";
        public static string LOGIN_FUNCTIONERR = "4";
    }    
    
    public class DBSQL
    {
		public String constr;
        public String cmdstatus;
        public DBSQL()
        {
			constr = System.Configuration.ConfigurationManager.ConnectionStrings["gtmasqlserver"].ConnectionString;
        }

        public recordset GetMy(String stcds)
        {
            recordset rd = new recordset();
            rd.cols = new ArrayList();
            rd.rows = new ArrayList();

            try
            {
                SqlConnection sc = new SqlConnection(constr);
                SqlCommand sm = new SqlCommand();
                sm.CommandType = CommandType.StoredProcedure;
                sm.CommandText = "[irri_getmy]";
                sm.Connection = sc;
                SqlParameter param0 = new SqlParameter("@stcds", stcds);
                sm.Parameters.Add(param0);
                sc.Open();
                SqlDataReader dr = sm.ExecuteReader();
                if (dr.HasRows)
                {
                    for (int i = 0; i < dr.FieldCount; i++) rd.cols.Add(dr.GetName(i));
                    while (dr.Read())
                    {
                        object[] ob = new object[dr.FieldCount];
                        dr.GetValues(ob);
                        rd.rows.Add(ob);
                    }
                }
                dr.Close();
                sc.Close();
            }
            catch (Exception e)
            {
                rd.name = e.ToString();
            }
            return rd;
        }

        public recordset GetProp(String stcd)
		{
            recordset rd = new recordset();
            rd.cols = new ArrayList();
            rd.rows = new ArrayList();
            
            try
            {
                SqlConnection sc = new SqlConnection(constr);
                SqlCommand sm = new SqlCommand();
                sm.CommandType = CommandType.StoredProcedure;
                sm.CommandText = "[irri_getprop]";
                sm.Connection = sc;
                SqlParameter param0 = new SqlParameter("@stcd", stcd);
                sm.Parameters.Add(param0);
                sc.Open();
                SqlDataReader dr = sm.ExecuteReader();
                if (dr.HasRows)
                {
                    for (int i = 0; i < dr.FieldCount; i++) rd.cols.Add(dr.GetName(i));
                    while (dr.Read())
                    {
                        object[] ob = new object[dr.FieldCount];
                        dr.GetValues(ob);
                        rd.rows.Add(ob);
                    }
                }
                dr.Close();
                sc.Close();
            }
            catch (Exception e)
            {
				rd.name= e.ToString();
            }
            return rd;
        }
        
        public recordset GetRealTime()
		{
            recordset rd = new recordset();
            rd.cols = new ArrayList();
            rd.rows = new ArrayList();
            
            try
            {
                SqlConnection sc = new SqlConnection(constr);
                SqlCommand sm = new SqlCommand();
                sm.CommandType = CommandType.StoredProcedure;
                sm.CommandText = "[irri_getrealtime]";
                sm.Connection = sc;
                sc.Open();
                SqlDataReader dr = sm.ExecuteReader();
                if (dr.HasRows)
                {
                    for (int i = 0; i < dr.FieldCount; i++) rd.cols.Add(dr.GetName(i));
                    while (dr.Read())
                    {
                        object[] ob = new object[dr.FieldCount];
                        dr.GetValues(ob);
                        rd.rows.Add(ob);
                    }
                }
                dr.Close();
                sc.Close();
            }
            catch (Exception e)
            {
				rd.name= e.ToString();
            }
            return rd;
        }
    }
}