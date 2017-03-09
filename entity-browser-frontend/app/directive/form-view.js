angular.module('xodus').directive('formView', [
    '$uibModal',
    '$location',
    'NavigationService',
    'ConfirmationService',
    function ($uibModal, $location, navigation, confirmation) {
        return {
            restrict: 'E',
            scope: {
                entityId: '=',
                entityTypeId: '='
            },
            replace: true,
            template: require('../templates/form-view.html'),
            link: function (scope, element) {
                scope.editMode = (scope.entityId === null);
                scope.toggleView = function () {
                    scope.editMode = !scope.editMode;
                };
                scope.cancel = function () {
                    if (scope.editMode) {
                        confirmExit(toSearch);
                    } else {
                        navigation.toType(scope.entityTypeId);
                    }
                };
                scope.getMessage = getMessage;

                scope.hasError = function (formName, inputName) {
                    var field = getForm(formName)[inputName];
                    return field && field.$invalid && field.$dirty;
                };
                scope.makeDirty = function (form) {
                    angular.forEach(Object.keys(form), function (key) {
                        var value = form[key];
                        if (value && angular.isFunction(value.$setDirty)) {
                            value.$setDirty(true);
                        }
                    });
                };
                scope.getForm = getForm;
                var cleanUp = scope.$on('$locationChangeStart', function (event, next, current) {
                        if (scope.editMode) {
                            confirmExit(function () {
                                // due to https://github.com/angular/angular.js/issues/8617
                                var path = next.substring($location.absUrl().length - $location.url().length);
                                $location.path(path);
                            });
                            event.preventDefault();
                        }
                    }
                );
                scope.$on('$destroy', cleanUp);

                function getMessage(formName, name) {
                    var field = getForm(formName)[name];
                    if (('undefined' === typeof(field)) || field.$valid) {
                        return undefined;
                    }

                    var message = '';
                    if (field.$error['number']) {
                        message += ' - not a number';
                    }
                    if (field.$error['min']) {
                        message += ' - too small';
                    }
                    if (field.$error['max']) {
                        message += ' - too large';
                    }
                    if (field.$error['pattern']) {
                        message += ' - not match pattern';
                    }
                    if (field.$error['duplicated']) {
                        message += ' - duplicated';
                    }
                    if (field.$error['required'] && !message) {
                        message += ' - required field';
                    }
                    return '\u2718' + message;
                }

                function getForm(name) {
                    return element.find('form[name="' + name + '"]').scope()[name];
                }

                function confirmExit(callback) {
                    return confirmation({
                        label: 'You can loose unsaved data',
                        message: 'Are you sure to proceed?',
                        action: 'Proceed'
                    }, function (result) {
                        if (result) {
                            scope.editMode = false;
                            callback();
                        }
                    });
                }

                function toSearch() {
                    navigation.toType(scope.entityTypeId);
                }
            }
        };
    }])
;
