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
 * Manuarii Stein (manuarii.stein@icescrum.com)
 * Nicolas Noullet (nnoullet@kagilum.com)
 * Jeroen Broekhuizen (Jeroen.Broekhuizen@quintiq.com)
 *
 */

package org.icescrum.web.presentation.app.project

import org.icescrum.core.domain.AcceptanceTest.AcceptanceTestState
import org.icescrum.core.support.ProgressSupport

import org.icescrum.core.utils.BundleUtils

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.icescrum.core.domain.User
import org.icescrum.core.domain.Sprint
import org.icescrum.core.domain.Task
import org.icescrum.core.domain.Story
import org.icescrum.core.domain.Product
import org.icescrum.core.domain.Release

@Secured('inProduct() or (isAuthenticated() and stakeHolder())')
class SprintPlanController {

    def springSecurityService
    def sprintService
    def taskService
    def userService

    def titleBarContent() {
        def currentProduct = Product.load(params.product)
        def sprint
        if (params.id) {
            sprint = (Sprint)Sprint.getInProduct(params.long('product'),params.long('id')).list()
        }
        def sprintsName = []
        def sprintsId = []
        currentProduct.releases?.sort({a, b -> a.orderNumber <=> b.orderNumber} as Comparator)?.each {
            sprintsName.addAll(it.sprints.collect {v -> "${it.name} - Sprint ${v.orderNumber}"})
            sprintsId.addAll(it.sprints.id)
        }
        render(template: 'window/titleBarContent',
                model: [sprintsId: sprintsId,
                        sprint: sprint,
                        sprintsName: sprintsName])
    }

    def toolbar() {
        def sprint
        if (params.id) {
            sprint = Sprint.getInProduct(params.long('product'),params.long('id')).list()
        }
        User user = (User) springSecurityService.currentUser
        render(template: 'window/toolbar',
                model: [hideDoneState: user.preferences.hideDoneState,
                        currentFilter: user.preferences.filterTask,
                        exportFormats: getExportFormats(),
                        sprint: sprint ?: null])
    }

    def index() {
        Sprint sprint

        User user = (User) springSecurityService.currentUser
        def currentProduct = Product.load(params.product)
        if (!params.id) {
            def release = currentProduct.releases.find {it.state == Release.STATE_WAIT}
            render(template: 'window/blank', model: [release: release ?: null])
            return
        }else{
            sprint = Sprint.getInProduct(params.long('product'),params.long('id')).list()
        }
        if (!sprint) {
            returnError(text:message(code: 'is.sprint.error.not.exist'))
            return
        }

        def hideDoneState = request.readOnly ? '' : """<span id="show-done-sprint-${sprint.parentRelease.id}-${sprint.orderNumber}" class="show-done-sprint ${sprint.state == Sprint.STATE_INPROGRESS ? '' : 'hidden'}">${
                                        is.link(action:"changeHideDoneState",
                                                controller:"${controllerName}",
                                                history:"false",
                                                remote:"true",
                                                onSuccess:"jQuery.icescrum.updateHideDoneState('${message(code: 'is.ui.sprintPlan.toolbar.showDoneState')}','${message(code: 'is.ui.sprintPlan.toolbar.hideDoneState')}');" ,
                                                id:"${params.id}",
                                                update:"window-content-${controllerName}",user.preferences.hideDoneState ? message(code: 'is.ui.sprintPlan.toolbar.showDoneState') : message(code: 'is.ui.sprintPlan.toolbar.hideDoneState'))
                                        } </span>"""

        def columns = [
                [key: (Task.STATE_WAIT), name: 'is.task.state.wait'],
                [key: (Task.STATE_BUSY), name: 'is.task.state.inprogress'],
                [key: (Task.STATE_DONE), name: 'is.task.state.done', html:hideDoneState]
        ]

        def stateSelect = BundleUtils.taskStates.collect {k, v -> "'$k':'${message(code: v)}'" }.join(',')

        def stories
        def recurrentTasks
        def urgentTasks
        def userid = params.long('userid')

        stories = Story.findStoriesFilter(sprint, null, user, userid).listDistinct()
        recurrentTasks = Task.findRecurrentTasksFilter(sprint, null, user, userid).listDistinct()
        urgentTasks = Task.findUrgentTasksFilter(sprint, null, user, userid).listDistinct()
        if (params.term && params.term != '') {
            def storiesMatchingTermOrTag = Story.searchAllByTermOrTag(currentProduct.id, params.term)
            stories = stories.intersect(storiesMatchingTermOrTag)
            def tasksMatchingTermOrTag = Task.searchAllByTermOrTag(currentProduct, params.term)
            recurrentTasks = recurrentTasks.intersect(tasksMatchingTermOrTag)
            urgentTasks = urgentTasks.intersect(tasksMatchingTermOrTag)
        }

        def template = params.viewType ? 'window/' + params.viewType : 'window/postitsView'
        render(template: template,
                model: [sprint: sprint,
                        stories: stories.findAll{it.state < Story.STATE_DONE},
                        storiesDone: stories.findAll{it.state == Story.STATE_DONE},
                        recurrentTasks: recurrentTasks,
                        urgentTasks: urgentTasks,
                        columns: columns,
                        stateSelect: stateSelect,
                        previousSprintExist: (sprint.orderNumber > 1 || sprint.parentRelease.orderNumber > 1) ?: false,
                        nextSprintExist: sprint.hasNextSprint,
                        displayUrgentTasks: sprint.parentRelease.parentProduct.preferences.displayUrgentTasks,
                        displayRecurrentTasks: sprint.parentRelease.parentProduct.preferences.displayRecurrentTasks,
                        limitValueUrgentTasks: sprint.parentRelease.parentProduct.preferences.limitUrgentTasks,
                        assignOnBeginTask:currentProduct.preferences.assignOnBeginTask,
                        urgentTasksLimited: (urgentTasks.findAll {it.state == Task.STATE_BUSY}.size() >= sprint.parentRelease.parentProduct.preferences.limitUrgentTasks),
                        user: user])
    }


    def updateTable() {

        withTask{ Task task ->
            if (params.boolean('loadrich')) {
                render(status: 200, text: task.notes ?: '')
                return
            }

            if (!params.table) {
                return
            }

            if (params.name != 'estimation') {
                if (task.state == Task.STATE_WAIT && params.name == "state" && params.task.state >= Task.STATE_BUSY) {
                    task.inProgressDate = new Date()
                } else if (task.state == Task.STATE_BUSY && params.name == "state" && params.task.state == Task.STATE_WAIT) {
                    task.inProgressDate = null
                }
                task.properties = params.task
            }
            else {
                params.task.estimation = params.task?.estimation?.replace(/,/,'.')
                task.estimation = params.task?.estimation?.isNumber() ? params.task.estimation.toFloat() : null
            }

            User user = (User) springSecurityService.currentUser
            taskService.update(task, user)

            def returnValue
            if (params.name == 'notes') {
                returnValue = wikitext.renderHtml(markup: 'Textile', text: task."${params.name}")
                returnValue = returnValue ?: ''
            } else if (params.name == 'estimation') {
                returnValue = task.estimation != null ? task.estimation.toString() : '?'
            } else if (params.name == 'state') {
                returnValue = g.message(code: BundleUtils.taskStates.get(task.state))
                returnValue = returnValue ?: ''
            } else if (params.name == 'description') {
                returnValue = task.description?.encodeAsHTML()?.encodeAsNL2BR()
                returnValue = returnValue ?: ''
            } else {
                returnValue = task."${params.name}".encodeAsHTML()
                returnValue = returnValue ?: ''
            }

            def version = task.isDirty() ? task.version + 1 : task.version
            render(status: 200, text: [version: version, value: returnValue] as JSON)
        }
    }

    def add() {
        Sprint sprint = (Sprint)Sprint.getInProduct(params.long('product'),params.long('id')).list()
        if (!sprint) {
            returnError(text:message(code: 'is.sprint.error.not.exist'))
            return
        }
        def stories = sprint ? Story.findAllByParentSprintAndStateLessThanEquals(sprint, Story.STATE_INPROGRESS, [sort: 'rank']) : []

        def selected = null
        if (params.story?.id && !(params.story.id in ['recurrent', 'urgent']))
            selected = Story.getInProduct(params.long('product'),params.long('story.id')).list()
        else if (params.story?.id && (params.story.id in ['recurrent', 'urgent']))
            selected = [id: params.story?.id]

        def selectList = []
        if (sprint.parentRelease.parentProduct.preferences.displayUrgentTasks)
            selectList << [id: 'urgent', name: ' ** ' + message(code: 'is.task.type.urgent') + ' ** ']
        if (sprint.parentRelease.parentProduct.preferences.displayRecurrentTasks)
            selectList << [id: 'recurrent', name: ' ** ' + message(code: 'is.task.type.recurrent') + ' ** ']
        selectList = selectList + stories

        render(template: 'window/manage', model: [
                sprint: sprint,
                stories: selectList,
                selected: selected,
                colorsLabels: BundleUtils.taskColorsSelect.values().collect { message(code: it) },
                colorsKeys: BundleUtils.taskColorsSelect.keySet().asList(),
                params: [product: params.product, id: sprint.id]
        ])
    }

    def edit() {
        withTask('subid'){Task task ->
            def selected = (task.type == Task.TYPE_RECURRENT) ? [id: 'recurrent'] : (task.type == Task.TYPE_URGENT) ? [id: 'urgent'] : task.parentStory
            def sprint = task.backlog
            def stories = Story.findAllByParentSprintAndStateLessThanEquals((Sprint) sprint, Story.STATE_INPROGRESS, [sort: 'rank'])

            def selectList = []
            if (sprint.parentRelease.parentProduct.preferences.displayRecurrentTasks)
                selectList << [id: 'recurrent', name: ' ** ' + message(code: 'is.task.type.recurrent') + ' ** ']
            if (sprint.parentRelease.parentProduct.preferences.displayUrgentTasks)
                selectList << [id: 'urgent', name: ' ** ' + message(code: 'is.task.type.urgent') + ' ** ']
            selectList = selectList + stories

            User user = (User) springSecurityService.currentUser
            def next = Task.findNextTask(task, user).list()[0]

            render(template: 'window/manage', model: [
                    task: task,
                    referrerUrl: params.referrerUrl,
                    stories: selectList,
                    selected: selected,
                    next: next?.id ?: '',
                    sprint: task.backlog,
                    colorsLabels: BundleUtils.taskColorsSelect.values().collect { message(code: it) },
                    colorsKeys: BundleUtils.taskColorsSelect.keySet().asList(),
                    params: [product: params.product, id: task.id]
            ])
        }
    }

    def editStory() {
        forward(action: 'edit', controller: 'story', params: [referrer: controllerName, id: params.id, product: params.product])
    }

    def doneDefinition(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        render(template: 'window/doneDefinition', model: [sprint: sprint])
    }

    @Secured('(productOwner() or scrumMaster()) and !archivedProduct()')
    def copyFromPreviousDoneDefinition(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        Sprint.withTransaction {
            def previousSprint = sprint.previousSprint
            if (!previousSprint) {
                returnError(text:message(code: 'is.sprint.error.doneDefinition.no.previous'))
            }
            sprint.doneDefinition = previousSprint.doneDefinition
            sprintService.update(sprint)
        }
        redirect(action: 'doneDefinition', params: [product: params.product, id: sprint.id])
    }

    def retrospective(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        render(template: 'window/retrospective', model: [sprint: sprint])
    }

    @Secured('(productOwner() or scrumMaster()) and !archivedProduct()')
    def copyFromPreviousRetrospective(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        Sprint.withTransaction {
            def previousSprint = sprint.previousSprint
            if (!previousSprint) {
                returnError(text:message(code: 'is.sprint.error.doneDefinition.no.previous'))
            }
            sprint.retrospective = previousSprint.retrospective
            sprintService.update(sprint)
        }
        redirect(action: 'retrospective', params: [product: params.product, id: sprint.id])
    }


    def changeFilterTasks() {
        if (!params.filter || !(params.filter in ['allTasks', 'myTasks', 'freeTasks', 'blockedTasks'])) {
            returnError(text:message(code: 'is.user.preferences.error.not.filter'))
            return
        }
        User user = (User) springSecurityService.currentUser
        user.preferences.filterTask = params.filter
        userService.update(user)
        redirect(action: 'index', params: [product: params.product, id: params.id])
    }

    def changeHideDoneState() {
        User user = (User) springSecurityService.currentUser
        user.preferences.hideDoneState = !user.preferences.hideDoneState
        userService.update(user)
        redirect(action: 'index', params: [product: params.product, id: params.id])
    }

    // TODO check rights
    def copyRecurrentTasksFromPreviousSprint(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        def tasks = sprintService.copyRecurrentTasksFromPreviousSprint(sprint)
        render(status: 200, contentType: 'application/json', text: tasks as JSON)
    }

    /**
     * Export the sprint backlog elements in multiple format (PDF, DOCX, RTF, ODT)
     */
    def print() {
        def currentProduct = Product.load(params.product)
        Sprint sprint = (Sprint)Sprint.getInProduct(params.long('product'),params.long('id')).list()
        def data
        def chart = null

        if (!sprint) {
            returnError(text:message(code: 'is.sprint.error.not.exist'))
            return
        }

        if (params.locationHash) {
            chart = processLocationHash(params.locationHash.decodeURL()).action
        }

        switch (chart) {
            case 'sprintBurndownRemainingChart':
                data = sprintService.sprintBurndownRemainingValues(sprint)
                break
            case 'sprintBurnupTasksChart':
                data = sprintService.sprintBurnupTasksValues(sprint)
                break
            case 'sprintBurnupStoriesChart':
                data = sprintService.sprintBurnupStoriesValues(sprint)
                break
            default:
                chart = 'sprintPlan'

                // Retrieve all the tasks associated to the sprint
                def stories = Story.findStoriesFilter(sprint, null, null).listDistinct()
                def tasks = Task.findRecurrentTasksFilter(sprint, null, null).listDistinct() + Task.findUrgentTasksFilter(sprint, null, null).listDistinct()
                stories.each {
                    tasks = tasks + it.tasks
                }
                // Workaround to force gorm to fetch the associated data (required for jasper to access the data)
                tasks.each {
                    it.parentStory?.name
                    it.responsible?.lastName
                    it.creator?.lastName
                }
                data = [
                        [
                                taskStateBundle: BundleUtils.taskStates,
                                tasks: tasks,
                                sprintBurndownRemainingChart: sprintService.sprintBurndownRemainingValues(sprint),
                                sprintBurnupTasksChart: sprintService.sprintBurnupTasksValues(sprint),
                                sprintBurnupStoriesChart: sprintService.sprintBurnupStoriesValues(sprint)
                        ]
                ]
                break
        }

        if (data.size() <= 0) {
            returnError(text:message(code: 'is.report.error.no.data'))
        } else if (params.get) {
            renderReport(chart ?: 'sprintPlan', params.format, data, currentProduct.name, ['labels.projectName': currentProduct.name])
        } else if (params.status) {
            render(status: 200, contentType: 'application/json', text: session?.progress as JSON)
        } else {
            session.progress = new ProgressSupport()
            def dialog = g.render(template: '/scrumOS/report')
            render(status: 200, contentType: 'application/json', text: [dialog:dialog] as JSON)
        }
    }

    def notes() {
        forward(action: "notesCached", params:params)
    }

    def notesCached(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        render(status:200,
               template: 'window/notes',
               model:[sprint:sprint,
                       tasks:sprint.tasks?.findAll{it.type == Task.TYPE_URGENT && it.state == Task.STATE_DONE},
                       technicalStories:sprint.stories?.findAll{it.type == Story.TYPE_TECHNICAL_STORY && it.state == Story.STATE_DONE},
                       userStories:sprint.stories?.findAll{it.type == Story.TYPE_USER_STORY && it.state == Story.STATE_DONE},
                       defectStories:sprint.stories?.findAll{it.type == Story.TYPE_DEFECT && it.state == Story.STATE_DONE}])
    }

    def printPostits(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        def _product = sprint.parentProduct
        def stories1 = []
        def stories2 = []
        def first = 0
        if (!sprint.stories) {
            returnError(text:message(code: 'is.report.error.no.data'))
            return
        } else if (params.get) {
            sprint.stories?.each {
                def testsByState = it.countTestsByState()
                def story = [
                        name: it.name,
                        id: it.uid,
                        effort: it.effort,
                        state: message(code: BundleUtils.storyStates[it.state]),
                        description: is.storyDescription([story: it, displayBR: true]),
                        notes: wikitext.renderHtml([markup: 'Textile'], it.notes).decodeHTML(),
                        type: message(code: BundleUtils.storyTypes[it.type]),
                        suggestedDate: it.suggestedDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.suggestedDate]) : null,
                        acceptedDate: it.acceptedDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.acceptedDate]) : null,
                        estimatedDate: it.estimatedDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.estimatedDate]) : null,
                        plannedDate: it.plannedDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.plannedDate]) : null,
                        inProgressDate: it.inProgressDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.inProgressDate]) : null,
                        doneDate: it.doneDate ? g.formatDate([formatName: 'is.date.format.short', timeZone: _product.preferences.timezone, date: it.doneDate ?: null]) : null,
                        rank: it.rank ?: null,
                        sprint: g.message(code: 'is.release') + " " + sprint.parentRelease.orderNumber + " - " + g.message(code: 'is.sprint') + " " + sprint.orderNumber,
                        creator: it.creator.firstName + ' ' + it.creator.lastName,
                        feature: it.feature?.name ?: null,
                        dependsOn: it.dependsOn?.name ? it.dependsOn.uid + " " + it.dependsOn.name : null,
                        permalink:createLink(absolute: true, mapping: "shortURL", params: [product: _product.pkey], id: it.uid),
                        featureColor: it.feature?.color ?: null,
                        nbTestsTocheck: testsByState[AcceptanceTestState.TOCHECK],
                        nbTestsFailed: testsByState[AcceptanceTestState.FAILED],
                        nbTestsSuccess: testsByState[AcceptanceTestState.SUCCESS]
                ]
                if (first == 0) {
                    stories1 << story
                    first = 1
                } else {
                    stories2 << story
                    first = 0
                }

            }
            renderReport('stories', params.format, [[product: _product.name, stories1: stories1 ?: null, stories2: stories2 ?: null]], _product.name)
        } else if (params.status) {
            render(status: 200, contentType: 'application/json', text: session?.progress as JSON)
        } else {
            session.progress = new ProgressSupport()
            def dialog = g.render(template: '/scrumOS/report', model: [sprint: sprint])
            render(status: 200, contentType: 'application/json', text: [dialog:dialog] as JSON)
        }
    }

    /**
     * Parse the location hash string passed in argument
     * @param locationHash
     * @return A Map
     */
    private processLocationHash(String locationHash) {
        def data = locationHash.split('/')
        return [
                controller: data[0].replace('#', ''),
                action: data.size() > 1 ? data[1] : null
        ]
    }

    @Secured('inProduct()')
    def addDocument(long product, long id) {
        Sprint sprint = Sprint.withSprint(product, id)
        def dialog = g.render(template:'/attachment/dialogs/documents', model:[bean:sprint, destController:'sprint'])
        render status: 200, contentType: 'application/json', text: [dialog: dialog] as JSON
    }
}