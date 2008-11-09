/**
 * Created on Nov 3, 2008
 * Author: Thies
 *
 * Copyright (C) 2007 TE-CON, All Rights Reserved.
 *
 * This Software is copyright TE-CON 2007. This Software is not open source by definition. The source of the Software is available for educational purposes.
 * TE-CON holds all the ownership rights on the Software.
 * TE-CON freely grants the right to use the Software. Any reproduction or modification of this Software, whether for commercial use or open source,
 * is subject to obtaining the prior express authorization of TE-CON.
 * 
 * thies@te-con.nl
 * TE-CON
 * Legmeerstraat 4-2h, 1058ND, AMSTERDAM, The Netherlands
 *
 */

package net.rrm.ehour.audit;

import java.util.Calendar;
import java.util.Date;

import net.rrm.ehour.audit.service.AuditService;
import net.rrm.ehour.domain.Audit;
import net.rrm.ehour.domain.AuditActionType;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.ui.session.EhourWebSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Auditable Aspect
 **/
@Component
@Aspect
public class AuditAspect
{
	private AuditService	auditService;
	
	/**
	 * Audit 
	 * @param pjp
	 * @param auditable
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(auditable)")
	public Object auditable(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable
	{
		Object returnObject;

		User user = EhourWebSession.getSession().getUser().getUser();

		try
		{
			returnObject = pjp.proceed();
		}
		catch (Throwable t)
		{
			auditService.persistAudit(createAudit(user, Boolean.FALSE, auditable.actionType(), pjp));
			
			throw t;
		}
		
		auditService.persistAudit(createAudit(user, Boolean.TRUE, auditable.actionType(), pjp));	
		
		return returnObject;
	}

	/**
	 * 
	 * @param user
	 * @param success
	 * @param action
	 * @param pjp
	 * @return
	 */
	private Audit createAudit(User user, Boolean success, AuditActionType auditActionType, ProceedingJoinPoint pjp)
	{
		StringBuilder parameters = new StringBuilder();
		
		int i = 0;
		
		for (Object object : pjp.getArgs())
		{
			parameters.append(i++ + ":");
			
			if (object instanceof Calendar)
			{
				parameters.append(((Calendar)object).getTime().toString());
			}
			else
			{
				parameters.append(object.toString());
			}
		}
		
		Audit audit = new Audit()
				.setUser(user)
				.setUserName(user != null ? user.getFullName() : null)
				.setDate(new Date())
				.setSuccess(success)
				.setAction(pjp.getSignature().toShortString())
				.setAuditActionType(auditActionType)
				.setParameters(parameters.toString())
				;

		return audit;

//		System.out.println(pjp.getSignature().toLongString());
//		System.out.println(pjp.getSignature().toShortString());
//		System.out.println(pjp.getTarget().getClass());
//		
//		System.out.println("user: " + EhourWebSession.getSession().getUser());
//		
//		

	}
	
	/**
	 * @param auditService the auditService to set
	 */
	@Autowired
	public void setAuditService(AuditService auditService)
	{
		this.auditService = auditService;
	}
}