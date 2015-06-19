<%@ WebHandler Language="C#" Class="irri.RealTimeHandler" %>

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

    public class RealTimeHandler : IHttpHandler, IReadOnlySessionState{
        
        public void ProcessRequest (HttpContext c) {
            c.Response.ContentType = "application/json";
            String cmd = c.Request["cmd"];
            if (cmd == "realtime"){
                OnCmdRealTime(c);
            }else if(cmd == "prop"){
                OnCmdProp(c);
            }
            else if (cmd == "my")
            {
                OnCmdMy(c);
            }
        }

        public bool IsReusable {
            get {
                return false;
            }
        }
        
        public void OnCmdProp(HttpContext c)
        {
            //Dictionary<String, object> myinfo = (Dictionary<String, object>)c.Session["myinfo"];
            objresponse ret = new objresponse(); ret.cmdstatus = objresponse.LOGIN_FAIL;
            //if (myinfo != null)
            {
                try
                {
                    String stcd = c.Request["stcd"];
                    if(stcd == "" || stcd == null) stcd = "10001001";
                    DBSQL d = new DBSQL();
                    ret.rd = d.GetProp(stcd);
                    ret.cmdstatus = objresponse.CMD_SUCESS;
                }
                catch (Exception e)
                {
                    ret.cmdstatus = objresponse.LOGIN_DBERROR;
                    c.Response.Write("err");
                }
            }
            JavaScriptSerializer serializer = new JavaScriptSerializer();
            c.Response.Write(serializer.Serialize(ret));                    
        }        
        
        public void OnCmdRealTime(HttpContext c)
        {
            //Dictionary<String, object> myinfo = (Dictionary<String, object>)c.Session["myinfo"];
            objresponse ret = new objresponse(); ret.cmdstatus = objresponse.LOGIN_FAIL;
            //if (myinfo != null)
            {
                try
                {
                    DBSQL d = new DBSQL();
                    ret.rd = d.GetRealTime();
                    ret.cmdstatus = objresponse.CMD_SUCESS;
                }
                catch (Exception e)
                {
                    ret.cmdstatus = objresponse.LOGIN_DBERROR;
                    c.Response.Write("err");
                }
            }
            JavaScriptSerializer serializer = new JavaScriptSerializer();
            c.Response.Write(serializer.Serialize(ret));                    
        }

        public void OnCmdMy(HttpContext c)
        {
            //Dictionary<String, object> myinfo = (Dictionary<String, object>)c.Session["myinfo"];
            objresponse ret = new objresponse(); ret.cmdstatus = objresponse.LOGIN_FAIL;
            //if (myinfo != null)
            {
                try
                {
                    DBSQL d = new DBSQL();
                    ret.rd = d.GetMy(c.Request["stcds"]);
                    ret.cmdstatus = objresponse.CMD_SUCESS;
                }
                catch (Exception e)
                {
                    ret.cmdstatus = objresponse.LOGIN_DBERROR;
                    c.Response.Write("err");
                }
            }
            JavaScriptSerializer serializer = new JavaScriptSerializer();
            c.Response.Write(serializer.Serialize(ret));
        }        

        
                
    }
}