%{--
- Copyright (c) 2015 Kagilum.
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

<is:modal title="${message(code: 'is.ui.team.menu')}"
          class="split-modal"
          footer="false">
    <div class="row">
        <ul class="left-panel col-sm-3 nav nav-list">
            <div class="input-group">
                <input type="text" ng-model="teamSearch" ng-change="searchTeams()" ng-model-options="{debounce: 300}" class="form-control" placeholder="todo.is.ui.search">
                <span class="input-group-btn">
                    <button class="btn btn-default" type="button"><span class="fa fa-search"></span></button>
                </span>
            </div>
                <li ng-class="{ 'current': team.id == currentTeam.id }" ng-repeat="currentTeam in teams">
                    <a ng-click="selectTeam(currentTeam)" href>{{ currentTeam.name }}</a>
                </li>
            <pagination boundary-links="true"
                        previous-text="&lsaquo;" next-text="&rsaquo;" first-text="&laquo;" last-text="&raquo;"
                        class="pagination-sm"
                        max-size="3"
                        total-items="totalTeams"
                        items-per-page="teamsPerPage"
                        ng-model="currentPage"
                        ng-change="searchTeams()">
            </pagination>
        </ul>
        <div class="right-panel col-sm-9" ng-switch="teamSelected()">
            <div ng-switch-default>
                <form ng-submit="save(newTeam)"
                      name="formHolder.newTeamForm"
                      show-validation
                      novalidate>
                    <div class="form-group">
                        <label for="team.name">${message(code:'is.ui.team.create.name')}</label>
                        <input required
                               ng-maxlength="100"
                               name="team.name"
                               ng-model="newTeam.name"
                               type="text"
                               class="form-control">
                    </div>
                    <div class="btn-toolbar pull-right">
                        <button class="btn btn-primary pull-right"
                                ng-disabled="!formHolder.newTeamForm.$dirty || formHolder.newTeamForm.$invalid"
                                tooltip="${message(code:'todo.is.ui.save')} (RETURN)"
                                tooltip-append-to-body="true"
                                type="submit">
                            ${message(code:'todo.is.ui.save')}
                        </button>
                    </div>
                </form>
                <div>
                    ${ message(code: 'is.ui.team.explanation') }
                </div>
            </div>
            <div ng-switch-when="true">
                <form ng-submit="update(team)"
                      name="formHolder.updateTeamForm"
                      show-validation
                      novalidate>
                    <div class="col-sm-12 form-group"
                         ng-switch="authorizedTeam('changeOwner', team)">
                        <label for="team.name">${message(code:'todo.is.ui.owner')}</label>
                        <span class="form-control-static"
                              ng-switch-default>{{ team.owner | userFullName }}</span>
                        <select class="form-control"
                                name="owner"
                                ng-switch-when="true"
                                ng-options="ownerCandidate | userFullName for ownerCandidate in ownerCandidates"
                                ng-model="team.owner"
                                ui-select2>
                        </select>
                    </div>
                    <div class="form-half">
                        <label for="team.name">${message(code:'todo.is.ui.name')}</label>
                        <input required
                               ng-maxlength="100"
                               name="team.name"
                               ng-model="team.name"
                               type="text"
                               class="form-control">
                    </div>
                    <div class="form-half">
                        <label for="member.search">${message(code:'todo.is.ui.select.member')}</label>
                        <p class="input-group typeahead">
                            <input autocomplete="off"
                                   type="text"
                                   name="member.search"
                                   id="member.search"
                                   focus-me="true"
                                   class="form-control"
                                   ng-model="member.name"
                                   typeahead="member as member.name for member in searchMembers($viewValue)"
                                   typeahead-loading="searchingMember"
                                   typeahead-wait-ms="250"
                                   typeahead-on-select="addTeamMember($item, $model, $label)"
                                   typeahead-template-url="select.member.html">
                            <span class="input-group-addon">
                                <i class="fa" ng-click="unSelectTeam()" ng-class="{ 'fa-search': !searchingMember, 'fa-refresh':searchingMember, 'fa-close':member.name }"></i>
                            </span>
                        </p>
                    </div>
                    <table ng-if="team.members.length" class="table table-striped table-responsive">
                        <thead>
                        <tr>
                            <th></th>
                            <th></th>
                            <th class="text-right">${message(code:'todo.is.ui.team.scrumMaster')}</th>
                        </tr>
                        </thead>
                        <tbody ng-repeat="member in team.members"
                               ng-include="'wizard.members.list.html'">
                        </tbody>
                    </table>
                    <div ng-if="team.members.length == 0">
                        ${message(code: 'todo.is.ui.team.no.members')}
                    </div>
                    <div class="btn-toolbar">
                        <button class="btn btn-primary pull-right"
                                ng-disabled="!formHolder.updateTeamForm.$dirty || formHolder.updateTeamForm.$invalid"
                                tooltip="${message(code:'todo.is.ui.update')}"
                                tooltip-append-to-body="true"
                                type="submit">
                            ${message(code:'todo.is.ui.update')}
                        </button>
                        <button class="btn btn-default pull-right"
                                tooltip-append-to-body="true"
                                tooltip="${message(code:'is.button.cancel')}"
                                type="button"
                                ng-click="cancel()">
                            ${message(code:'is.button.cancel')}
                        </button>
                        <button ng-if="authorizedTeam('delete', team) && team.products_count == 0" class="btn btn-danger pull-left"
                                ng-click="confirm({ message: '${message(code: 'is.confirm.delete')}', callback: delete, args: [team] })"
                                tooltip-placement="left"
                                tooltip-append-to-body="true"
                                tooltip="${message(code:'todo.is.ui.delete')}">
                            ${message(code:'todo.is.ui.delete')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</is:modal>