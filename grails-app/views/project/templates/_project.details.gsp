<script type="text/ng-template" id="project.details.html">
    <div class="row">
        <h4 class="col-md-6">{{ project.name }} ({{ project.pkey }})</h4>
        <h4 class="col-md-6 text-right"><i class="fa fa-users"></i> {{ project.team.name }}</h4>
    </div>
    <div class="row">
        <div class="col-md-6" ng-bind-html="(project.description_html ? project.description_html : '<p>${message(code: 'todo.is.ui.project.nodescription')}</p>') | sanitize"></div>
        <div class="col-md-6">PLACEHOLDER CHART</div>
    </div>
    <div class="row" style="margin-bottom: 10px;">
        <div class="col-md-4"><i class="fa fa-user"></i> {{ projectMembersCount }} ${ message(code: 'todo.is.ui.members') }</div>
        <div class="col-md-4"><i class="fa fa-sticky-note"></i> {{ project.stories_count }} ${ message(code: 'todo.is.ui.stories') }</div>
        <div class="col-md-4"><i class="fa fa-calendar"></i> {{ project.releases_count }} ${ message(code: 'todo.is.ui.releases') }</div>
    </div>
    <progress class="form-control-static form-bar"
              tooltip-append-to-body="true"
              tooltip="{{ release.name }}"
              tooltip-placement="top"
              max="release.duration">
        <bar ng-repeat="sprint in release.sprints"
             class="{{ $last ? 'last-bar' : '' }}"
             tooltip-append-to-body="true"
             tooltip-template="'sprint.tooltip.html'"
             tooltip-placement="bottom"
             type="{{ { 1: 'default', 2: 'progress', 3: 'done' }[sprint.state] }}"
             value="sprint.duration">
            #{{ sprint.orderNumber }}
        </bar>
        <div class="progress-empty" ng-if="release.sprints != undefined && release.sprints.length == 0">${message(code: 'todo.is.ui.nosprint')}</div>
    </progress>
    <div class="row">
        <div class="col-md-6">{{ release.startDate }}</div>
        <div class="col-md-6 text-right">{{ release.endDate }}</div>
    </div>
</script>