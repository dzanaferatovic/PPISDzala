package com.example.controllers;



import com.example.models.*;
import com.example.repositories.DepartmentRepository;
import com.example.repositories.IncidentRepository;
import com.example.repositories.RequestRepository;
import com.example.repositories.StatusRepository;
import com.example.repositories.UserRepository;

import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

/**
 * Created by Admira on 08-May-17.
 */
@RestController
@RequestMapping("/requests")
public class RequestController {

    @Autowired
    private UserRepository userr;
    @Autowired
    private RequestRepository reqr;
    @Autowired
    private StatusRepository statusr;
    @Autowired
    private IncidentRepository incidentr;
    @Autowired
    private DepartmentRepository departmentr;

    @RequestMapping("/{iduser}/addRequest")
    public void	 add( @PathVariable("iduser") long iduser, @RequestBody ReqBody req) throws Exception
    {

        RegisteredUser user=userr.findById(iduser);
        Status status=statusr.findByStatus("created");


        if(req.description==null)
        {
            throw new Exception("Trazena polja za unos incidenta nisu ispravno popunjena.");
        }

        Request newreq=new Request();
        newreq.setDescription(req.description);
        newreq.setUser(user);
        newreq.setStatus(status);
        newreq.setCreated(new Date()); //stavice na sadasnje vrijeme
        newreq.setContactMethod(req.contactMethod);
        newreq.setReportMethod(req.reportMethod);
        if(req.title!=null) newreq.setTitle(req.title);
        
        reqr.save(newreq);
    }

    @RequestMapping(value = "/userRequest", method = RequestMethod.GET)
    @ResponseBody
    public List<Request> findRequestsByUserId(@RequestParam("userid") Long id) {
        RegisteredUser user=userr.findById(id);
        return reqr.getByUser(user);
    }

    @RequestMapping("/{id}/delete")
    public void deleteRequest(@PathVariable("id") long id) throws Exception
    {
        Request i=reqr.findById(id);

        if(i.getDescription()==null)
        {
            throw new Exception("Ne postoji incident sa tim id-em");
        }

        reqr.delete(i);
    }
    
    @RequestMapping("/getbyid/{id}")
    public Request getRequest(@PathVariable("id") long id){
    	return reqr.findOne(id);
    }
    
    @RequestMapping("/active")
    public List<Request> getActiveRequests(){
    	return reqr.getActiveRequests();
    }
    
    @RequestMapping("/closed")
    public List<Request> getClosedRequests(){
    	return reqr.getClosedRequests();
    }
    @RequestMapping("/add")
    public void add(@RequestBody Request rb){
    	
    	Request r = new Request();
    	Status s=statusr.findByStatus("Nerijesen");
    	r.setTitle(rb.getTitle());
    	r.setUser(rb.getUser());
    	r.setDescription(rb.getDescription());
    	r.setContactMethod(rb.getContactMethod());
    	r.setDepartment(rb.getDepartment());
    	r.setPriority(rb.getPriority());
    	r.setUrgency(rb.getUrgency());
    	r.setReportMethod(rb.getReportMethod());
    	r.setStatus(s);
    	r.setDepartment(rb.getDepartment());
    	reqr.save(r);
    }
    @RequestMapping("/convert/{id}")
    public void pretvoriUIncident(@PathVariable("id") long id){
    	Request falseRequest=reqr.findOne(id);
    	Incident realIncident= new Incident();
    	realIncident.setTitle(falseRequest.getTitle());
    	realIncident.setDescription(falseRequest.getDescription());
    	realIncident.setContactMethod(falseRequest.getContactMethod());
    	realIncident.setReportMethod(falseRequest.getReportMethod());
    	realIncident.setTaken(0);
    	realIncident.setCreated(falseRequest.getCreated());
    	realIncident.setUser(falseRequest.getUser());
    	realIncident.setPriority(0);
    	realIncident.setRepetition(0);
    	realIncident.setDepartment(falseRequest.getDepartment());
    	realIncident.setUrgency(falseRequest.getUrgency());
    	realIncident.setStatus(statusr.findByStatus("Poslan"));
    	incidentr.save(realIncident);
    	falseRequest.setStatus(statusr.findByStatus("Pogresno prijavljen"));
    	reqr.save(falseRequest);
    	
    }
    
    @RequestMapping("/reject/{id}")
    public void reject(@PathVariable("id") long id){
    	Request request = reqr.findById(id);
    	request.setStatus(statusr.findByStatus("Odbijen"));
    	reqr.save(request);
    }
    
    @RequestMapping("/partialupdate/{id}")
    public void partialUpdate(@PathVariable("id") long id,@RequestBody Request r){
    	Request request=reqr.findById(id);
    	request.setPriority(r.getPriority());
    	request.setStatus(statusr.findByStatus("U obradi"));
    	request.setDepartment(r.getDepartment());
    	reqr.save(request);
    	
    }

	@RequestMapping("/yearlystatistics")
	public StatisticsResponse yearlyStatistics(@RequestParam("year") int year){
		StatisticsResponse body=new StatisticsResponse();
		body.all=this.yearly(year);
		body.active=this.yearlyActive(year);
		body.closed=this.yearlyClosed(year);
		
		return body;
	}
	
	@RequestMapping("/monthlystatistics")
	public StatisticsResponse yearlyStatistics(@RequestParam("year") int year,@RequestParam("month") int month){
		StatisticsResponse body=new StatisticsResponse();
		body.all=this.monthly(year, month);
		body.active=this.monthlyactive(year, month);
		body.closed=this.monthlyclosed(year, month);
		
		return body;
	}
	
	@RequestMapping("/yearlyall")
	public int yearly(@RequestParam("year") int year){
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(0);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(11);
		d2.setDate(31);
		System.out.println(d2);
		
		return reqr.countRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/monthlyall")
	public int monthly(@RequestParam("year") int year,@RequestParam("month") int month){
		int niz[]={31,28,31,30,31,30,31,31,30,31,30,31};
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(month-1);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(month-1);
		
		d2.setDate(niz[month-1]);
		System.out.println(d2);
		
		return reqr.countRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/yearlyclosed")
	public int yearlyClosed(@RequestParam("year") int year){
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(0);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(11);
		d2.setDate(31);
		System.out.println(d2);
		
		return reqr.countClosedRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/monthlyclosed")
	public int monthlyclosed(@RequestParam("year") int year,@RequestParam("month") int month){
		int niz[]={31,28,31,30,31,30,31,31,30,31,30,31};
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(month-1);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(month-1);
		
		d2.setDate(niz[month-1]);
		System.out.println(d2);
		
		return reqr.countClosedRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/yearlyactive")
	public int yearlyActive(@RequestParam("year") int year){
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(0);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(11);
		d2.setDate(31);
		System.out.println(d2);
		
		return reqr.countActiveRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/monthlyactive")
	public int monthlyactive(@RequestParam("year") int year,@RequestParam("month") int month){
		int niz[]={31,28,31,30,31,30,31,31,30,31,30,31};
		Date d1=new Date();
		d1.setYear(year-1900);
		d1.setDate(1);
		d1.setMonth(month-1);
		System.out.println(d1);
		
		Date d2=new Date();
		d2.setYear(year-1900);
		d2.setDate(5);
		d2.setMonth(month-1);
		
		d2.setDate(niz[month-1]);
		System.out.println(d2);
		
		return reqr.countActiveRequestsByDate(d1, d2);
		
	}
	
	@RequestMapping("/reportrequest")
	public void reportRequest(@RequestBody UserRequestBody rb) throws ServletException{
		RegisteredUser user=userr.findById(rb.userId);
		Status status=statusr.findByStatus("Nerijesen");
		Department d=departmentr.findByName("Odjel za podrsku korisnicima");
		
		if(rb.description==null || rb.description.equals("")){
			throw new ServletException("Nedostaje opis zahtjeva");
		}
		
		if(rb.title==null || rb.title.equals("")){
			throw new ServletException("Nedostaje naziv zahtjeva");
		}
		
		Request r = new Request();
		r.setTitle(rb.title);
		r.setDescription(rb.description);
		r.setUser(user);
		r.setStatus(status);
		r.setContactMethod(rb.contactMethod);
		r.setPriority(0);
		r.setReportMethod(3);
		r.setDepartment(d);
		r.setUrgency(0);
		
		reqr.save(r);
	}
    
    @RequestMapping("/all")
    public List<Request> all(){
    	return (List<Request>)reqr.findAll();
    }

    private static class ReqBody{
        public String description;
        public int reportMethod;
        public int contactMethod;
        public String title;
    }
    
	@SuppressWarnings("unused")
	public static class StatisticsResponse{
		public int active;
		public int closed;
		public int all;
	}
	
	@SuppressWarnings("unused")
	private static class UserRequestBody{
		public long userId;
		public String title;
		public String description;
		public int contactMethod;
	}
}

