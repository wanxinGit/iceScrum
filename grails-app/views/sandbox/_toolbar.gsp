%{--
- Copyright (c) 2014 Kagilum SAS.
-
- This file is part of iceScrum.
-
- iceScrum is free software: you can redistribute it and/or modify
- it under the terms of the GNU Affero General Public License as published by
- the Free Software Foundation, either version 3 of the License.
-
- iceScrum is distributed in the hope that it will be useful,
- but WITHOUT ANY WARRANTY; without even the implied warranty of
- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
- GNU General Public License for more details.
-
- You should have received a copy of the GNU Affero General Public License
- along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
-
- Authors:
-
- Vincent Barrier (vbarrier@kagilum.com)
- Nicolas Noullet (nnoullet@kagilum.com)
--}%
<div class="btn-group">
    <a type="button"
       ng-if="authorizedStory('create')"
       tooltip="${message(code:'todo.is.ui.new')}"
       tooltip-append-to-body="true"
       tooltip-placement="right"
       ng-click="goToNewStory()"
       class="btn btn-primary">
        <span class="fa fa-plus"></span>
    </a>
    <button type="button"
            tooltip="${message(code:'todo.is.ui.toggle.grid.list')}"
            tooltip-append-to-body="true"
            tooltip-placement="right"
            ng-click="view.asList = !view.asList"
            class="btn btn-default">
        <span class="fa fa-th" ng-class="{'fa-th-list': view.asList, 'fa-th': !view.asList}"></span>
    </button>
    <div class="btn-group"
         dropdown
         is-open="orderBy.status"
         tooltip-append-to-body="true"
         tooltip="${message(code:'todo.is.ui.sort')}">
        <button class="btn btn-default dropdown-toggle" dropdown-toggle type="button">
            <span id="sort">{{ orderBy.current.name }}</span>
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" role="menu">
            <li role="menuitem" ng-repeat="order in orderBy.values">
                <a ng-click="orderBy.current = order; orderBy.status = false;" href>{{ order.name }}</a>
            </li>
        </ul>
    </div>
    <button type="button" class="btn btn-default"
            ng-click="orderBy.reverse = !orderBy.reverse"
            tooltip="${message(code:'todo.is.ui.order')}"
            tooltip-append-to-body="true">
        <span class="fa fa-sort-amount{{ orderBy.reverse ? '-desc' : '-asc'}}"></span>
    </button>
</div>
<div class="btn-group" tooltip-append-to-body="true" dropdown tooltip="${message(code:'todo.is.ui.export')}">
    <button class="btn btn-default dropdown-toggle" dropdown-toggle type="button">
        <span class="fa fa-download"></span>&nbsp;<span class="caret"></span>
    </button>
    <ul class="dropdown-menu"
        role="menu">
        <g:each in="${is.exportFormats()}" var="format">
            <li role="menuitem">
                <a href="${createLink(action:format.action?:'print',controller:format.controller?:controllerName,params:format.params)}"
                   ng-click="print($event)">${format.name}</a>
            </li>
        </g:each>
        <entry:point id="${controllerName}-toolbar-export" model="[product:params.product, origin:controllerName]"/>
    </ul>
</div>
<div class="btn-group pull-right">
    <entry:point id="${controllerName}-${actionName}-toolbar-right"/>
    <g:if test="${params?.printable}">
        <button type="button"
                class="btn btn-default"
                tooltip="${message(code:'is.ui.window.print')} (P)"
                tooltip-append-to-body="true"
                tooltip-placement="left"
                ng-click="print($event)"
                ng-href="{{ viewName }}/print"
                hotkey="{'P': hotkeyClick }"><span class="fa fa-print"></span>
        </button>
    </g:if>
    <g:if test="${params?.fullScreen}">
        <button type="button"
                class="btn btn-default"
                ng-show="!app.isFullScreen"
                ng-click="fullScreen()"
                tooltip="${message(code:'is.ui.window.fullscreen')} (F)"
                tooltip-append-to-body="true"
                tooltip-placement="left"
                hotkey="{'F': fullScreen }"><span class="fa fa-expand"></span>
        </button>
        <button type="button"
                class="btn btn-default"
                ng-show="app.isFullScreen"
                tooltip="${message(code:'is.ui.window.fullscreen')}"
                tooltip-append-to-body="true"
                tooltip-placement="left"
                ng-click="fullScreen()"><span class="fa fa-compress"></span>
        </button>
    </g:if>
</div>
<div class="clearfix"></div>
<div class="views" style="padding-top:5px;padding-bottom:5px;font-size: 0;text-align:center;">
    <div style="float:left; margin-right:5px;width:104px;height:64px;overflow:hidden;font-size: 0;border:1px solid #dcdcdc;border-radius:2px;padding:2px;display:inline-block;">
        <g:each var="test" in="${stories}">
            <div style="opacity:0.6; width: 10px; height: 8px; background-color:${test.feature?.color?:'yellow'}; margin:2px; display: inline-block"></div>
        </g:each>
    </div>
    <div style="float:left; margin-right:5px;width:104px;height:64px;overflow:hidden;font-size: 0;border:1px solid #dcdcdc;border-radius:2px;padding:2px;display:inline-block;">
        <g:each var="test" in="${stories2}">
            <div style="opacity:0.6; width: 10px; height: 8px; background-color:${test.feature?.color?:'yellow'}; margin:2px; display: inline-block"></div>
        </g:each>
    </div>
    <div style="float:left; width:104px;height:64px;overflow:hidden;font-size: 0;border:1px solid #dcdcdc;border-radius:2px;padding:2px;display:inline-block;">
        <g:each var="test" in="${stories3}">
            <div style="opacity:0.6; width: 10px; height: 8px; background-color:${test.feature?.color?:'yellow'}; margin:2px; display: inline-block"></div>
        </g:each>
    </div>
</div>
