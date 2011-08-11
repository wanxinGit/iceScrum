/*
 * Copyright (c) 2010 iceScrum Technologies.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * iceScrum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors:
 *
 * Vincent Barrier (vbarrier@kagilum.com)
 * Stephane Maldini (stephane.maldini@icescrum.com)
 */

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.icescrum.core.security.MethodScrumExpressionHandler
import org.icescrum.core.security.ScrumDetailsService
import org.icescrum.core.security.WebScrumExpressionHandler
import org.icescrum.core.support.MenuBarSupport
import org.icescrum.web.security.ScrumAuthenticationProcessingFilter
import org.icescrum.web.upload.AjaxMultipartResolver
import org.icescrum.cache.ProjectCacheResolver
import org.icescrum.cache.UserCacheResolver
import org.icescrum.cache.BacklogElementCacheResolver
import org.icescrum.cache.RoleAndLocaleKeyGenerator
import org.icescrum.cache.LocaleKeyGenerator
import org.icescrum.cache.UserKeyGenerator
import org.icescrum.cache.UserProjectCacheResolver
import grails.plugin.springcache.web.key.WebContentKeyGenerator
import org.icescrum.cache.IceScrumCacheResolver

beans = {

    projectCacheResolver(ProjectCacheResolver){
        springcacheCacheManager = ref('springcacheCacheManager')
        springSecurityService = ref('springSecurityService')
        grailsApplication = ref('grailsApplication')
    }
    backlogElementCacheResolver(BacklogElementCacheResolver){
        springcacheCacheManager = ref('springcacheCacheManager')
        springSecurityService = ref('springSecurityService')
        grailsApplication = ref('grailsApplication')
    }
    userCacheResolver(UserCacheResolver){
        springcacheCacheManager = ref('springcacheCacheManager')
        springSecurityService = ref('springSecurityService')
        grailsApplication = ref('grailsApplication')
    }
    userProjectCacheResolver(UserProjectCacheResolver){
        springcacheCacheManager = ref('springcacheCacheManager')
        springSecurityService = ref('springSecurityService')
        grailsApplication = ref('grailsApplication')
    }

    userKeyGenerator(UserKeyGenerator) {
        contentType = true
        springSecurityService = ref('springSecurityService')
    }
    localeKeyGenerator(LocaleKeyGenerator) {
        contentType = true
    }
    roleAndLocaleKeyGenerator(RoleAndLocaleKeyGenerator) {
        contentType = true
        securityService = ref('securityService')
    }
    springcacheDefaultKeyGenerator(WebContentKeyGenerator){
        contentType = true
    }

    authenticationProcessingFilter(ScrumAuthenticationProcessingFilter) {
    def conf = SpringSecurityUtils.securityConfig
        authenticationManager = ref('authenticationManager')
        sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('authenticationFailureHandler')
        rememberMeServices = ref('rememberMeServices')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        filterProcessesUrl = conf.apf.filterProcessesUrl
        usernameParameter = conf.apf.usernameParameter
        passwordParameter = conf.apf.passwordParameter
        continueChainBeforeSuccessfulAuthentication = conf.apf.continueChainBeforeSuccessfulAuthentication
        allowSessionCreation = conf.apf.allowSessionCreation
        postOnly = conf.apf.postOnly
    }

    webExpressionHandler(WebScrumExpressionHandler) {
        roleHierarchy = ref('roleHierarchy')
    }

    expressionHandler(MethodScrumExpressionHandler) {
        parameterNameDiscoverer = ref('parameterNameDiscoverer')
        permissionEvaluator = ref('permissionEvaluator')
        roleHierarchy = ref('roleHierarchy')
        trustResolver = ref('authenticationTrustResolver')
    }

    menuBarSupport(MenuBarSupport) {innerBean ->
        innerBean.autowire = "byName"
    }

    userDetailsService(ScrumDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    multipartResolver(AjaxMultipartResolver) {
        maxInMemorySize = 10240
        maxUploadSize = 1024000000
    }
}