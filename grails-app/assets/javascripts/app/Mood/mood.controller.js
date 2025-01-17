/*
 * Copyright (c) 2015 Kagilum.
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
 * Marwah Soltani (msoltani@kagilum.com)
 */

controllers.controller('moodCtrl', ['$scope', 'MoodService', 'MoodFeelingsByName', function ($scope, MoodService, MoodFeelingsByName) {
    $scope.save = function (feelingString) {
        var mood = {feeling: MoodFeelingsByName[feelingString]};
        MoodService.save(mood)
            .then(function (savedMood) {
                $scope.mood = savedMood;
                $scope.alreadySavedToday = true;
                $scope.listMoodsIfNeeded();
            });
    };
    $scope.listMoodsIfNeeded = function() {
        if ($scope.alreadySavedToday) {
            MoodService.listByUser()
                .then(function (moods) {
                    $scope.moods = moods;
                });
        }
    };
    // Init
    $scope.moods = [];
    MoodService.isAlreadySavedToday()
        .then(function (data) {
            $scope.alreadySavedToday = data.value;
            $scope.listMoodsIfNeeded();
        });
}]);


